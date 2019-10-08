package robot.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;

public class HatchMechanism {

    private DoubleSolenoid hatchSolenoid;

    public HatchMechanism() {
        hatchSolenoid = new DoubleSolenoid(0, 2);
    }

    public void place()
    {
        hatchSolenoid.set(DoubleSolenoid.Value.kForward);
    }

    public void retract()
    {
        hatchSolenoid.set(DoubleSolenoid.Value.kReverse);
    }


}

