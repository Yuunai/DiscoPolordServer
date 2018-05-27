package discopolord;

import discopolord.database.DiscoDataSource;
import discopolord.entity.Contact;
import discopolord.entity.User;
import discopolord.server.Server;


public class Main {

    public static void main(String[] args) {

        Server server = new Server();
        server.start();

//        DiscoDataSource discoDataSource = DiscoDataSource.getInstance();
//
//        discoDataSource.open();
//
//        User user = discoDataSource.getUser("Email@mail.com", "passwd");
//
//        System.out.println(user.toString());
//
//        try {
////            discoDataSource.saveOrUpdateUser(new User("SuperUserro", "UserName", "Passwood", "Email@mail.com"));
////
////            discoDataSource.saveOrUpdateUser(new User(1, "SuperUser", "UserName", "Passwd", "Email@mail.com"));
//
//            discoDataSource.saveOrUpdateUserContact(new Contact(1, 3, Contact.CONTACT_TYPE_FRIEND));
//
//            discoDataSource.saveOrUpdateUserContact(new Contact(3, 1, Contact.CONTACT_TYPE_BLOCKED));
//            discoDataSource.saveOrUpdateUserContact(new Contact(1, 3, Contact.CONTACT_TYPE_BLOCKED));
//
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        discoDataSource.close();
    }


}
