package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;


@Autonomous(name = "Cheese", group = "Test")
public class CheeseCode extends LinearOpMode {

    HardwareTestBot bot = new HardwareTestBot();


    @Override
    public void runOpMode() throws InterruptedException {
        bot.init(hardwareMap);
        waitForStart();
        while(opModeIsActive()) {
            /*
            move seems to have issues because the setPower method works find but move method doesn't work
             */
            bot.drive.move(0, 0.5f, 0.5f);
            bot.drive.waitForDone();
            sleep(1000);
            bot.drive.move(180,0.5f, 0.5f);
            bot.drive.waitForDone();
            sleep(1000);
            bot.drive.move(90, 0.5f, 0.5f);
            bot.drive.waitForDone();
            sleep(1000);
            bot.drive.move(270, 0.5f, 0.5f);
            bot.drive.waitForDone();
            sleep(1000);
            bot.drive.stopRobot();
            break;
        }
    }
}
