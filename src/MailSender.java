import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailSender {

    private String to = PropertiesHandler.getInstance("common.properties").readValue("mailTo");
    private String pass = PropertiesHandler.getInstance("common.properties").readValue("mailPass");
    private String fromAddress = PropertiesHandler.getInstance("common.properties").readValue("mailFrom");
    String subject = PropertiesHandler.getInstance("common.properties").readValue("mailSubject");
    String name = "";

    public MailSender(String name){
        this.name = name;
    }

    public void sendMail(){
        String msg = "This is a reminder, "+name+"'s birthday is today. They wrote on your wall, now write on theirs";

        Properties props = System.getProperties();
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.user", "username");
        props.put("mail.smtp.password", "password");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", true);

        Session session = Session.getInstance(props,null);
        MimeMessage message = new MimeMessage(session);

        System.out.println("Port: "+session.getProperty("mail.smtp.port"));

        // Create the email addresses involved
        try {
            InternetAddress from = new InternetAddress(fromAddress);
            message.setSubject(subject);
            message.setFrom(from);
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            // Create a multi-part to combine the parts
            Multipart multipart = new MimeMultipart("alternative");

            // Create your text message part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(msg);

            // Add the text part to the multipart
            multipart.addBodyPart(messageBodyPart);

            // Create the html part
//		        messageBodyPart = new MimeBodyPart();
//		        String htmlMessage = "Our html text";
//		        messageBodyPart.setContent(htmlMessage, "text/html");

            // Add html part to multi part
            multipart.addBodyPart(messageBodyPart);

            // Associate multi-part with message
            message.setContent(multipart);

            // Send message
            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", fromAddress, pass);
            System.out.println("Transport: "+transport.toString());
            transport.sendMessage(message, message.getAllRecipients());

        } catch (AddressException e) {
            Driver.addComment(e.toString());
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
            Driver.addComment(e.toString());
        }
    }

    public static void main(String[] args){
        MailSender sender = new MailSender("jeez");
        sender.sendMail();
    }

}
