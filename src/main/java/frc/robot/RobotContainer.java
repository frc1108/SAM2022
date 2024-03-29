// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.util.Map;

import com.revrobotics.CANSparkMax.IdleMode;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants.*;
import frc.robot.commands.Shoot;
import frc.robot.commands.shooter.AutoAim;
import frc.robot.subsystems.VisionSubsystem;
import frc.robot.commands.ShootOnce;
import frc.robot.commands.auto.FourBallShort;
import frc.robot.commands.auto.OneBallAuto;
import frc.robot.commands.auto.TwoBallAuto;
import frc.robot.commands.auto.WIPRudeTwoBallAuto;
import frc.robot.commands.auto.RudeTwoBallAuto;
import frc.robot.subsystems.ClimberSubsystem;
import frc.robot.subsystems.ColorSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import io.github.oblarg.oblog.annotations.Log;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.LEDSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.StartEndCommand;
import edu.wpi.first.wpilibj2.command.WaitCommand;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  @Log private final DriveSubsystem m_drive = new DriveSubsystem();
  @Log private final ShooterSubsystem m_shooter = new ShooterSubsystem();
  @Log private final IntakeSubsystem m_intake = new IntakeSubsystem();
  @Log private final ClimberSubsystem m_climber = new ClimberSubsystem();
  @Log private final LEDSubsystem m_led = new LEDSubsystem();
  private final ColorSubsystem m_color = new ColorSubsystem();
  @Log public final VisionSubsystem m_vision = new VisionSubsystem();

  private final XboxController m_driverController = new XboxController(OIConstants.kDriverControllerPort);
  private final XboxController m_operatorController = new XboxController(OIConstants.kOperatorControllerPort);

  private final SendableChooser<Command> autoChooser = new SendableChooser<>();
 // private final SendableChooser<Double> delayChooser = new SendableChooser<>();
  private NetworkTableEntry delay;
  private boolean runLight = false;
  //private double autoDelay = 0;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();
    delay = Shuffleboard.getTab("Live").add("Auto Delay", 0).withWidget(BuiltInWidgets.kNumberSlider).withProperties((Map.of("Min", 0, "Max", 10, "Block increment", 1))).getEntry();
    autoChooser.setDefaultOption("Nothing", new WaitCommand(5));
    autoChooser.addOption("2 Ball Auto", new TwoBallAuto(m_drive, m_shooter, m_intake));
    autoChooser.addOption("Rude 2 Ball Auto", new RudeTwoBallAuto(m_drive, m_shooter, m_intake));
    autoChooser.addOption("TEST Rude 2 Ball Auto", new WIPRudeTwoBallAuto(m_drive, m_shooter, m_intake));
    autoChooser.addOption("4 Ball Auto", new FourBallShort(m_drive, m_shooter, m_intake));
    autoChooser.addOption("1 Ball Auto", new OneBallAuto(m_drive, m_shooter, m_intake));
    //Shuffleboard.getTab("Live").add("Auto Mode",autoChooser).withSize(2, 1);
    SmartDashboard.putData("Auto Chooser",autoChooser);

    
    
    m_drive.setDefaultCommand(
        new RunCommand(
            () -> m_drive.arcadeDrive(
                    m_driverController.getLeftY(),
                    m_driverController.getRightX()),
            m_drive).withName("Drive Manual"));
    m_drive.setMaxOutput(DriveConstants.kNormalDriveMaxSpeed);
    m_intake.setDefaultCommand(
        new RunCommand(
            () -> m_intake.intake(MathUtil.applyDeadband(m_operatorController.getLeftY(), OIConstants.kOperatorLeftDeadband)),
            m_intake));
    m_climber.setDefaultCommand(
        new RunCommand(
            () -> m_climber.climber(MathUtil.applyDeadband(m_operatorController.getRightY(), OIConstants.kOperatorRightDeadband)),
            m_climber));
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    new JoystickButton(m_operatorController, XboxController.Button.kA.value)
        .whenPressed(new Shoot(m_shooter, 0.45));
    new JoystickButton(m_operatorController, XboxController.Button.kLeftBumper.value)
        .whileHeld(new RunCommand(()->m_shooter.kick(50), m_shooter));
    new JoystickButton(m_operatorController, XboxController.Button.kRightBumper.value)
        .whileHeld(new RunCommand(()->m_shooter.kick(-25), m_shooter));
/*     new JoystickButton(m_driverController, XboxController.Button.kRightBumper.value)
        .whileActiveOnce(new RunCommand(
            () -> m_drive.arcadeDrive(
                    (m_driverController.getLeftY()*OIConstants.kDriverSlowModifier),
                    (m_driverController.getRightX())*OIConstants.kDriverSlowModifier),
            m_drive
        )).whileActiveOnce(new StartEndCommand(
            () -> m_drive.changeIdleMode(IdleMode.kCoast),
            () -> m_drive.changeIdleMode(IdleMode.kBrake))); */
    new JoystickButton(m_driverController, XboxController.Button.kRightBumper.value)
        .whenPressed(()->m_drive.setMaxOutput(DriveConstants.kBurstDriveMaxSpeed))
        .whenReleased(()->m_drive.setMaxOutput(DriveConstants.kSlowDriveMaxSpeed));
    
    //Below may or may not be a drift button
    new JoystickButton(m_driverController, XboxController.Button.kLeftBumper.value)
        .whileActiveOnce(new StartEndCommand(
            () -> m_drive.changeIdleMode(IdleMode.kCoast),
            () -> m_drive.changeIdleMode(IdleMode.kBrake)));
    
    new JoystickButton(m_operatorController, XboxController.Button.kY.value)
        .whenPressed(new InstantCommand(()->m_intake.toggleExtension(), m_intake));
    new JoystickButton(m_operatorController, XboxController.Button.kBack.value)
        .whenPressed(new InstantCommand(()->m_climber.toggleTilt(), m_climber));
    new POVButton(m_operatorController, 0)
        .whenPressed(new InstantCommand(()->m_shooter.plateUp()));
    new POVButton(m_operatorController, 180)
        .whenPressed(new InstantCommand(()->m_shooter.plateDown()));
    new POVButton(m_operatorController, 90)
        .whenPressed(new ShootOnce(m_shooter));
    new JoystickButton(m_operatorController, XboxController.Button.kX.value)
        .whenPressed(new InstantCommand(()->m_shooter.toggleTilt()));
    new POVButton(m_driverController, 0)
        .whenPressed(()->runLight=false)
        .whenPressed(new WaitCommand(0.1)
        .andThen(new InstantCommand(()->m_led.setRed(),m_led)));
    new POVButton(m_driverController, 90)
        .whenPressed(()->runLight=false)
        .whenPressed(new WaitCommand(0.1)
        .andThen(new InstantCommand(()->m_led.setColor(255, 100, 0),m_led)));
    new POVButton(m_driverController, 180)
        .whenPressed(()->runLight=false)
        .whenPressed(new WaitCommand(0.1)
        .andThen(new InstantCommand(()->m_led.setColor(0, 0, 255),m_led)));
    new POVButton(m_driverController, 270)
        .whenPressed(new InstantCommand(()->runLight = true))
        .whenPressed(new RunCommand(()->m_led.chasingHSV(24)).withInterrupt(()->runLight==false));

    // While driver holds the A button Auto Aim to the High Hub and range to distance    
    new JoystickButton(m_driverController, XboxController.Button.kA.value)
        .whileActiveOnce(new AutoAim(m_drive,m_vision,true,m_driverController));
 
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An ExampleCommand will run in autonomous
    return new SequentialCommandGroup(new WaitCommand(delay.getDouble(0)), autoChooser.getSelected());
    //return autoChooser.getSelected();
  }

  public void setOperatorRumble() {
    var goodRumble = ((m_color.getBlueFrontMatch()&&DriverStation.getAlliance()==DriverStation.Alliance.Blue))||
                     ((m_color.getRedFrontMatch()&&DriverStation.getAlliance()==DriverStation.Alliance.Red));
    var badRumble = ((m_color.getBlueFrontMatch()&&DriverStation.getAlliance()==DriverStation.Alliance.Red))||
                     ((m_color.getRedFrontMatch()&&DriverStation.getAlliance()==DriverStation.Alliance.Blue));
    m_operatorController.setRumble(GenericHID.RumbleType.kLeftRumble, goodRumble ? 0.2 : 0 );
    m_operatorController.setRumble(GenericHID.RumbleType.kRightRumble, badRumble ? 1 : 0 );
  }

  public void reset(){
    m_drive.resetOdometry(new Pose2d());
  }
}
