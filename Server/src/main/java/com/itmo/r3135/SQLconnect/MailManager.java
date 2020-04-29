package com.itmo.r3135.SQLconnect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailManager {
    static final Logger logger = LogManager.getLogger("MailManager");
    private Session session;
    private String username;
    private String password;
    private String host;
    private int port;
    private boolean auth;

    public MailManager(String username, String password, String host, int port, boolean auth) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.auth = auth;
    }

    public boolean initMail() {
        logger.info("Mail Manager connect...");
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", host);
            prop.put("mail.smtp.port", port);
            prop.put("mail.smtp.auth", auth);
            prop.put("mail.smtp.socketFactory.port", port);
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            session = Session.getInstance(prop,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });
        } catch (Exception e) {
            logger.fatal("Mail Manager ERROR!");
            return false;
        }
        logger.info("Mail good connect!");
        return true;
    }

    public boolean sendMail(String eMail) {
        //session.setDebug(true);
        //пока тестовый текст☺♂☺
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(eMail)
            );
            message.setSubject("MESSAGE");
            message.setText("Message,"
                    + "\n\n Message!");
            Transport.send(message);

        } catch (MessagingException e) {
            logger.error("Send email message error!");
            return false;
        }
        return true;
    }

    public boolean sendMailHTML(String eMail) {
        //session.setDebug(true);
        //пока тестовый текст
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(eMail)
            );
            message.setSubject("Thank you for registering in our database");
            String text = new String();
            text = "<p>&nbsp;</p>\n" +
                    "<h3 style=\"text-align: center; color: #3f7320;\"><span style=\"border-bottom: 4px solid #c82828;\">Спасибо за регистрацию на нашей площадке</span>&nbsp;</h3>\n" +
                    "<!-- Этот комментарий виден только в редакторе исходного кода -->\n" +
                    "<p style=\"text-align: center;\"><strong>Наша команда выражает огромное спасибо за использоване наших приложений!<br />Мы развиваем наше приложение, в скором времени мы добавим графический интерфейс.</strong><strong></strong></p>\n" +
                    "<table class=\"demoTable\" style=\"height: 47px;\">\n" +
                    "<thead>\n" +
                    "<tr style=\"height: 18px;\">\n" +
                    "<td style=\"height: 18px; width: 437px;\"><span style=\"color: #c82828;\">login</span>:&nbsp;</td>\n" +
                    "<td style=\"height: 18px; width: 437px;\"><span style=\"color: #c82828;\">email</span>:&nbsp;</td>\n" +
                    "</tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n" +
                    "<tr style=\"height: 29px;\">\n" +
                    "<td style=\"height: 29px; width: 437px;\">вставить логин</td>\n" +
                    "<td style=\"height: 29px; width: 437px;\">вставить email</td>\n" +
                    "</tr>\n" +
                    "</tbody>\n" +
                    "</table>\n" +
                    "<h1 style=\"text-align: center;\"><strong>&nbsp;Немножко старых мемасов</strong><img src=\"https://sun9-12.userapi.com/c850016/v850016901/12720e/InQbhg3GPGU.jpg\" width=\"1000\" height=\"623\" /></h1>\n" +
                    "<p><img src=\"https://sun9-7.userapi.com/c847021/v847021125/1a3055/uiyQ8Qvgb9g.jpg\" width=\"1000\" height=\"562\" /><img src=\"https://sun9-8.userapi.com/c850016/v850016901/127218/SERc5qdOTKk.jpg\" width=\"1000\" height=\"625\" /></p>\n" +
                    "<p>&nbsp; &nbsp; &nbsp; &nbsp; Держите ещё: можете приколоть своих приятелей с помощью&nbsp;<a target=\"_blank\" rel=\"nofollow noopener\" href=\"https://geekprank.com/\">GeekPrank</a>.</p>☺☺";

            message.setContent(text,"text/html");
            Transport.send(message);

        } catch (MessagingException e) {
            logger.error("Send email message error!");
            return false;
        }
        return true;
    }
}
