// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

// my imports

import edu.wpi.first.wpilibj.xrp.XRPMotor;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.xrp.XRPServo;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.xrp.XRPReflectanceSensor;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kTimerAuto = "Timer Auto";
  private static final String kFollowBlackSensors = "Follow Black Auto w/ Sensors";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  
  private final XRPMotor leftDrive = new XRPMotor(0);
  private final XRPMotor rightDrive = new XRPMotor(1);
  private final DifferentialDrive drive = new DifferentialDrive(leftDrive, rightDrive);
  private final Timer mTimer = new Timer();
  private final XRPServo backServo = new XRPServo(4);
  private final XboxController mController = new XboxController(0);

  double driveSpeed = 1;

  private final XRPReflectanceSensor mReflectanceSensor = new XRPReflectanceSensor();
  private static final double FOLLOW_BLACK_THRESHOLD = 0.70; // Allowable distance difference (in meters)
  
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Timer Auto", kTimerAuto);
    m_chooser.addOption("Follow Black with Sensors", kFollowBlackSensors);
    SmartDashboard.putData("Auto choices", m_chooser);

    rightDrive.setInverted(true);
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    mTimer.start();
    mTimer.reset();
    backServo.setPosition(1);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kFollowBlackSensors:
      // Read sensor values
      double leftSensorValue = mReflectanceSensor.getLeftReflectanceValue();
      double rightSensorValue = mReflectanceSensor.getRightReflectanceValue(); 

      // Set boolean values based on thresholds

      boolean leftOnLine = (leftSensorValue > FOLLOW_BLACK_THRESHOLD);
      boolean rightOnLine = (rightSensorValue > FOLLOW_BLACK_THRESHOLD);

      SmartDashboard.putBoolean("leftOnline", leftOnLine);
      SmartDashboard.putBoolean("rightOnline", rightOnLine);
    
      // Line-following logic
      if (!leftOnLine && !rightOnLine) {
          // Lost the line, search by spinning in place
          drive.tankDrive(0.5, -0.5);
          SmartDashboard.putString("Action", "Spinning in place: " + .5 + -.5);
      } else if (leftOnLine && !rightOnLine) {
          // Turn left, slow left motor
          drive.tankDrive(0.2, 0.6);
          SmartDashboard.putString("Action", "Turning left L:" + .2 + " R:" + .6); 
      } else if (!leftOnLine && rightOnLine) {
          // Turn right, slow right motor
          drive.tankDrive(0.6, 0.2);
          SmartDashboard.putString("Action", "Turning right L:" + .6 + " R:" + .2); 
      } else if (leftOnLine && rightOnLine) {
          // Move forward
          drive.tankDrive(0.5, 0.5);
          SmartDashboard.putString("Action", "Moving forward L:" + .5 + " R:" + .5);
      }
        break;
      case kTimerAuto:
      default:
        // Put default auto code here
        if (mTimer.get() < 2.8) { 
          // drive forward & servo in
          drive.tankDrive(.6, .52);
        } 
        else if (mTimer.get() < 3.9) {
          drive.tankDrive(-.6, .6);
        }
        else if (mTimer.get() < 6.1) {
          drive.tankDrive(-.6, -.6);
        }
        else if (mTimer.get() < 6.4) {
          backServo.setPosition(.3);
        }
        else if (mTimer.get() < 7.4) {
          backServo.setPosition(1);
        }
        else {
          // move the servo in
          drive.tankDrive(0, 0);
        }
        break;
      }
    }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    
    if (mController.getLeftBumper()){
      driveSpeed = .6;
    }
    else if (mController.getRightBumper()){
      driveSpeed = 1;
    }
    
    //drive.tankDrive(-mController.getLeftY(), -mController.getRightY());
    drive.arcadeDrive(-mController.getLeftY() * driveSpeed, -mController.getRightX() * driveSpeed);

    if (mController.getAButton()) {
      backServo.setPosition(1);
    }
    else if (mController.getYButton()) {
      backServo.setPosition(0);
    }
    else if (mController.getBButton()) {
      backServo.setPosition(.5);
    }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}
}
