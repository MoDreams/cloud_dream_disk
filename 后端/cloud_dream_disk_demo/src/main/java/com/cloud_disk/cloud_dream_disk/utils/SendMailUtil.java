package com.cloud_disk.cloud_dream_disk.utils;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class SendMailUtil {
    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String Send;
    /*
    * ${AcceptMail} 用户名
    * ${affair} 当前操作的事情,如：注册、修改密码
    * ${Context} 验证码
    * ${ActiveTime} 有效时间
    * ${Time} 发送时间
    * */
    private static String CaptChaTemplate = "<div style=\"display: flex;justify-content: center;background-color:#212429;\">\n" +
            "        <div style=\"width: 100%; padding: 0 5%;color: #c5c8cf;\">\n" +
            "            <h1 style=\"display: flex;justify-content: center;color: #fff;\">云梦盘验证码</h1>\n" +
            "            <br>\n" +
            "            <div>\n" +
            "                <h4 style=\"color: #fff;\">尊敬的用户&nbsp;&nbsp;&nbsp;${AcceptMail}</h4>\n" +
            "                <h4 style=\"color: #fff;\">您好!</h4>\n" +
            "                <p>您正在申请发送<span style=\"color: #fff;font-size: 24px;\"><b>&nbsp;&nbsp;${affair}&nbsp;&nbsp;</b></span>验证码：</p>\n" +
            "                <p>为了验证账号安全,请在验证码输入框中输入下列验证码：</p>\n" +
            "                <div style=\"width: 100%;background-color: #000; padding: 2% 0;\">\n" +
            "                    <b\n" +
            "                        style=\"font-size: 25px;color: aqua;display: flex;justify-content: center;align-items: center;\">${Context}</b>\n" +
            "                </div>\n" +
            "                <p><b style=\"color: #fff;\">验证码涉及您的个人账号安全,请勿向他人透露</b></p>\n" +
            "                <p>该条验证码将于${ActiveTime}后失效</p>\n" +
            "                <p>如果本次验证码并非由您发起,请务必告诉我们,由此给你带来的不便敬请谅解。</p>\n" +
            "                <p style=\"float: right;\">\n" +
            "                    <b style=\"float: right;color: #fff;\">云梦盘</b>\n" +
            "                    <br>\n" +
            "                    <br>\n" +
            "                    ${Time}\n" +
            "                </p>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>";

    public static SendMailUtil sendMailUtil;

    @PostConstruct
    public void Send(){
        sendMailUtil = this;
    }
    public Boolean SendMail(String AcceptMail,String Subject,String Context){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(Send);
        message.setTo(AcceptMail);
        message.setSubject(Subject);
        message.setText(Context);
        try {
            javaMailSender.send(message);
        }catch (Exception e){
            return false;
        }
        return true;
    }
    public Boolean RegistryCaptChaMail(String AcceptMail,String Subject,String Context,String type,String ActiveTime) throws MessagingException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String SendTemplate = CaptChaTemplate.replace("${AcceptMail}",AcceptMail)
                .replace("${affair}", type)
                .replace("${Context}",Context)
                .replace("${ActiveTime}",ActiveTime)
                .replace("${Time}", dateFormat.format(new Date()));

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message,true);
        mimeMessageHelper.setFrom(Send);
        mimeMessageHelper.setTo(AcceptMail);
        mimeMessageHelper.setSubject(Subject);
        mimeMessageHelper.setText(SendTemplate,true);
        try {
            javaMailSender.send(message);
        }catch (Exception e){
            return false;
        }
        return true;

    }
}
