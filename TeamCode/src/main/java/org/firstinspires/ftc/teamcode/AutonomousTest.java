package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name = "AutonomousTest", group = "Test")
public class AutonomousTest extends LinearOpMode {

    private HardwareTestBot robot = new HardwareTestBot();
    private GoldLooker goldLooker;

    @Override
    public void runOpMode() {
        goldLooker = new GoldLooker(hardwareMap);
        waitForStart();
    }

    public void DropFromLander() {
        robot.Hooke.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.Hooke.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.Hooke.setTargetPosition(1000);
        robot.Hooke.setPower(1);
        while (robot.Hooke.isBusy()) ; //wait until target position is reached
        robot.Hooke.setPower(0);
        robot.Hooke.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void KnockOffGold() {
        goldLooker.start();
        robot.drive.move(0, .8f, .5f);
        robot.drive.move(-90, .8f, 15f / 36);
        robot.drive.waitForDone();
        robot.drive.resetCoords();
        int i;
        for (i = 0; i < 3; i++) {
            int look = goldLooker.look();
            if (look == 1) {
                robot.drive.move(0, .5f, .5f);
                robot.drive.move(180, .5f, .5f);
                robot.drive.waitForDone();
                break;
            }
            if (i != 2)
                robot.drive.move(90, .8f, 15f / 36);
            robot.drive.waitForDone();
        }
        robot.drive.move(90, .8f, (1 - i) * 15f / 36);

    }

    public void AlignWithPicture() {
    }

    public void PutMarkerInDepot() {

    }

    public void ParkInCrater() {

    }
}
