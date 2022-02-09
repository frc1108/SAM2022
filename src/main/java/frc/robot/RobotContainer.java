// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax.IdleMode;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import frc.robot.Constants.*;
import frc.robot.commands.Cycling;
import frc.robot.commands.FlipPlate;
import frc.robot.commands.Shoot;
import frc.robot.commands.auto.PickupOne;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.StartEndCommand;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final DriveSubsystem m_drive = new DriveSubsystem();
  private final ShooterSubsystem m_shooter = new ShooterSubsystem();
  private final IntakeSubsystem m_intake = new IntakeSubsystem();
  
  private final XboxController m_driverController = new XboxController(OIConstants.kDriverControllerPort);
  private final XboxController m_operatorController = new XboxController(OIConstants.kOperatorControllerPort);

  private final SendableChooser<Command> autoChooser = new SendableChooser<>();


  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();

    autoChooser.setDefaultOption("Pickup One Cargo", new PickupOne(m_drive));
    m_drive.setDefaultCommand(
        new RunCommand(
            () -> m_drive.arcadeDrive(
                    m_driverController.getLeftY(),
                    m_driverController.getRightX()),
            m_drive));
    m_intake.setDefaultCommand(
        new RunCommand(
            () -> m_intake.intake(MathUtil.applyDeadband(m_operatorController.getLeftY(), OIConstants.kOperatorLeftDeadband)),
            m_intake));
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    //testing kick and shoot code

    new JoystickButton(m_operatorController, XboxController.Button.kB.value)
        .toggleWhenActive(new StartEndCommand(()->m_shooter.shoot(35), ()->m_shooter.stopShoot()));
    new JoystickButton(m_operatorController, XboxController.Button.kA.value)
        .whenPressed(new Shoot(m_shooter, 54, 8));
    new JoystickButton(m_operatorController, XboxController.Button.kLeftBumper.value)
        .whileHeld(new RunCommand(()->m_shooter.kick(50), m_shooter));
    new JoystickButton(m_operatorController, XboxController.Button.kRightBumper.value)
        .whileHeld(new RunCommand(()->m_shooter.kick(-25), m_shooter));
    new JoystickButton(m_driverController, XboxController.Button.kRightBumper.value)
        .whileActiveOnce(new RunCommand(
            () -> m_drive.arcadeDrive(
                    (m_driverController.getLeftY()*OIConstants.kDriverSlowModifier),
                    (m_driverController.getRightX())*OIConstants.kDriverSlowModifier),
            m_drive
        )).whileActiveOnce(new StartEndCommand(
            () -> m_drive.changeIdleMode(IdleMode.kCoast),
            () -> m_drive.changeIdleMode(IdleMode.kBrake)));
    
    //Below may or may not be a drift button
    new JoystickButton(m_driverController, XboxController.Button.kLeftBumper.value)
        .whileActiveOnce(new StartEndCommand(
            () -> m_drive.changeIdleMode(IdleMode.kCoast),
            () -> m_drive.changeIdleMode(IdleMode.kBrake)));
    
    new JoystickButton(m_operatorController, XboxController.Button.kY.value)
        .whenPressed(new InstantCommand(()->m_intake.toggleExtension(), m_intake));
    new POVButton(m_operatorController, 0)
        .whenPressed(new InstantCommand(()->m_shooter.plateUp()));
    new POVButton(m_operatorController, 180)
        .whenPressed(new InstantCommand(()->m_shooter.plateDown()));
    new JoystickButton(m_operatorController, XboxController.Button.kX.value)
        .whenPressed(new InstantCommand(()->m_shooter.toggleTilt()));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An ExampleCommand will run in autonomous
    return null;
  }

  public void reset(){
    m_drive.resetOdometry(new Pose2d());
  }
}
