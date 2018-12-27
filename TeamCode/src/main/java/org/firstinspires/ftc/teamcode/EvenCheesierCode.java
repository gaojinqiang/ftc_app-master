package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@Autonomous(name = "EvenCheesier", group = "Test")
public class EvenCheesierCode extends LinearOpMode {

    HardwareTestBot bot = new HardwareTestBot();

    @Override
    public void runOpMode() throws InterruptedException {
        bot.init(hardwareMap);

        bot.leftFront.setPower(0.5);
        bot.leftBack.setPower(0.5);
        bot.rightFront.setPower(0.5);
        bot.rightBack.setPower(0.4);
        sleep(1000);
        bot.leftFront.setPower(-0.5);
        bot.leftBack.setPower(-0.5);
        bot.rightFront.setPower(-0.5);
        bot.rightBack.setPower(-0.4);
        sleep(1000);
        bot.leftFront.setPower(0.5);
        bot.leftBack.setPower(-0.5);
        bot.rightFront.setPower(-0.5);
        bot.rightBack.setPower(0.4);
        sleep(1000);
        bot.leftFront.setPower(-0.5);
        bot.leftBack.setPower(0.5);
        bot.rightFront.setPower(0.5);
        bot.rightBack.setPower(-0.4);
        sleep(1000);
        bot.leftFront.setPower(0);
        bot.leftBack.setPower(0);
        bot.rightFront.setPower(0);
        bot.rightBack.setPower(0);

    }
}
