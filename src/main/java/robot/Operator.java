package robot;

import robot.subsystems.*;

public class Operator {
    public static double ARM_TOP_ROCKET_BALL;
    public static double ARM_MID_ROCKET_BALL;
    public static double ARM_BOT_ROCKET_BALL;

    public static double WRIST_TOP_ROCKET_BALL;
    public static double WRIST_MID_ROCKET_BALL;
    public static double WRIST_BOT_ROCKET_BALL;
    
    public static double ARM_MID_ROCKET_HATCH;
    public static double ARM_LOW_HATCH;
    
    public static double WRIST_MID_ROCKET_HATCH;
    public static double WRIST_LOW_HATCH;
    
    public static double ARM_CARGO_BALL;
    
    public static double WRIST_CARGO_BALL;
    
    public static double ARM_GND_HATCH;
    public static double WRIST_GND_HATCH;
    
    public static double ARM_STARTUP;
    public static double WRIST_STARTUP;

    // Range of arm and wrist
    public static double WRIST_LOW_RANGE;
    public static double WRIST_HIGH_RANGE;

    public static double ARM_LOW_RANGE;
    public static double ARM_HIGH_RANGE;

    // Tolerance for limiting override range
    public static final double OVERRIDE_TOLERANCE = 5; //Should always be positive

    //Speeds of wheels - Negative speed push ball out
    public static final double WHEELS_SPEED_IN = 0.7;
    public static final double WHEELS_SPEED_OUT = -0.7;

    public static final int PID_ADJUST_SCALE = 5;

    Controller op;
    Arm arm;
    HatchMechanism hatch;
    Wheels wheels;
    Wrist wrist;

    public Operator(int port) {
        op = new Controller(port);
        arm = new Arm();
        hatch = new HatchMechanism();
        wheels = new Wheels();
        wrist = new Wrist();

        ARM_STARTUP = getArmAngle();
        WRIST_STARTUP = getWristAngle();

        ARM_TOP_ROCKET_BALL = ARM_STARTUP + 989;
        ARM_MID_ROCKET_BALL = ARM_STARTUP + 420;
        ARM_BOT_ROCKET_BALL = ARM_STARTUP + 200;

        ARM_MID_ROCKET_HATCH = ARM_STARTUP + 389;
        ARM_LOW_HATCH = ARM_STARTUP + 200;

        ARM_CARGO_BALL = ARM_STARTUP + 765;
        ARM_GND_HATCH = ARM_STARTUP + 313;
        
        ARM_LOW_RANGE = ARM_STARTUP;
        ARM_HIGH_RANGE = ARM_TOP_ROCKET_BALL;

        WRIST_TOP_ROCKET_BALL = WRIST_STARTUP - 1548;
        WRIST_MID_ROCKET_BALL = WRIST_STARTUP - 816;
        WRIST_BOT_ROCKET_BALL = WRIST_STARTUP - 630;

        WRIST_MID_ROCKET_HATCH = WRIST_STARTUP - 450;
        WRIST_LOW_HATCH = WRIST_STARTUP - 65;

        WRIST_CARGO_BALL = WRIST_STARTUP - 1782;
        WRIST_GND_HATCH = WRIST_STARTUP - 1645;

        WRIST_LOW_RANGE = WRIST_CARGO_BALL;
        WRIST_HIGH_RANGE = WRIST_STARTUP;
    }

    public void runOpControls() {
        wheelControl();
        armWristOverride();
        armWristControl();
        hatchControl();

        //System.out.println(wrist.pidOutput());o
        System.out.println("position "+ wrist.getPot().get());
        //System.out.println("set Pos "+ ARM_LOW_HATCH);
    }

    public void resetPID() {
        arm.reset();
        wrist.reset();
    }

    public double getArmAngle() {
        return arm.getPot().get();
    }

    public double getWristAngle() {
        return wrist.getPot().get();
    }

    private void wheelControl() {
        if (getWristAngle() > (WRIST_STARTUP - OVERRIDE_TOLERANCE)) {
            if(op.getRightBumper() || op.getLeftBumper())
                System.out.println("Wheels cannot spin as the wrist is too close to the arm");
            
            wheels.stopWheels();

            return; //Exits from the function before the speed can be set
        }

        if (op.getRightBumper()) {
            wheels.runWheels(WHEELS_SPEED_IN);
        }
        else if (op.getLeftBumper()) {
            wheels.runWheels(WHEELS_SPEED_OUT);
        }
        else {
            wheels.stopWheels();
        }
    }

    private void armWristOverride() {
        // Wrist override controlled by right stick
        if (op.getRightStickButton()) {
            if ((op.getRightYAxis() < 0) && (getWristAngle() < (WRIST_HIGH_RANGE - OVERRIDE_TOLERANCE))) {
                wrist.override(-op.getRightYAxis() / 2);
                System.out.println("Wrist going up");
            }
            else if ((op.getRightYAxis() > 0) && (getWristAngle() > (WRIST_LOW_RANGE + OVERRIDE_TOLERANCE))) {
                wrist.override(-op.getRightYAxis() / 2);
                System.out.println("Wrist going down");
            }
            else {
                wrist.stopWrist();
                System.out.println("WRIST AT EDGE OF OVERRIDE RANGE");
            }
        }
        else {
            if (!wrist.isPIDEnabled()) {
                wrist.stopWrist();
            }
            else {
                if(Math.abs(op.getRightYAxis())>0.1) {
                    if(((wrist.getSetpoint() + (-op.getRightYAxis())*PID_ADJUST_SCALE) < WRIST_HIGH_RANGE) && (((wrist.getSetpoint() + (-op.getRightYAxis())*PID_ADJUST_SCALE) > WRIST_LOW_RANGE))) {
                        wrist.setPosition(wrist.getSetpoint() + (-op.getRightYAxis())*PID_ADJUST_SCALE);
                    }
                }
            }
        }

        // Arm override controlled by left stick
        if (op.getLeftStickButton()) {
            double leftYAxis = -op.getLeftYAxis();
            if (((leftYAxis > 0) && (getArmAngle() < (ARM_HIGH_RANGE - OVERRIDE_TOLERANCE)))) {
                arm.override(leftYAxis*0.3);
                System.out.println("Arm going up");
            }
            else if ((op.getLeftYAxis() < 0) && (getArmAngle() > (ARM_LOW_RANGE + OVERRIDE_TOLERANCE))) {
                arm.override(leftYAxis*0.3);
                System.out.println("Arm going down");
            }
            else {
                arm.stopArm();
                System.out.println("ARM AT EDGE OF OVERRIDE RANGE");
            }
        }
        else {
            if (!arm.isPIDEnabled()) {
                arm.stopArm();
            }
            else {
                if(Math.abs(op.getLeftYAxis())>0.1) {
                    if(((arm.getSetpoint() + (-op.getLeftYAxis())*PID_ADJUST_SCALE) < ARM_HIGH_RANGE) && (((arm.getSetpoint() + (-op.getLeftYAxis())*PID_ADJUST_SCALE) > ARM_LOW_RANGE))) {
                        arm.setPosition(arm.getSetpoint() + (-op.getLeftYAxis())*PID_ADJUST_SCALE);
                    }
                }
            }
        }
    }

    private void armWristControl() {
        if (op.getOButton()) {
            arm.setPosition(ARM_MID_ROCKET_HATCH);
            wrist.setPosition(WRIST_MID_ROCKET_HATCH);
        }
        else if (op.getXButton()) {
            arm.setPosition(ARM_LOW_HATCH);
            wrist.setPosition(WRIST_LOW_HATCH);
        }
        else if (op.getSquareButton()) {
            arm.setPosition(ARM_STARTUP);
            wrist.setPosition(WRIST_STARTUP);
        }
        else if (op.getDPadUp()) {
            arm.setPosition(ARM_TOP_ROCKET_BALL);
            wrist.setPosition(WRIST_TOP_ROCKET_BALL);
        }
        else if (op.getDPadLeft()) {
            arm.setPosition(ARM_MID_ROCKET_BALL);
            wrist.setPosition(WRIST_MID_ROCKET_BALL);
        }
        else if (op.getDPadDown()) {
            arm.setPosition(ARM_BOT_ROCKET_BALL);
            wrist.setPosition(WRIST_BOT_ROCKET_BALL);
        }
        else if (op.getDPadRight()) {
            arm.setPosition(ARM_CARGO_BALL);
            wrist.setPosition(WRIST_CARGO_BALL);
        }
    }

    private void hatchControl() {
        if (op.getTriangleButton()) {
            hatch.place();
        }
        else {
            hatch.retract();
        }
    }
  
    public Controller getController() {
        return op;
    }
}
