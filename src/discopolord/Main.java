package discopolord;

import discopolord.database.DiscoDataSource;
import discopolord.entity.Relation;
import discopolord.entity.User;


public class Main {

    public static void main(String[] args) {

//        Server server = new Server();
//        server.start();

        DiscoDataSource discoDataSource = DiscoDataSource.getInstance();

        discoDataSource.open();

        User user = discoDataSource.getUser("Email@mail.com", "passwd");

        System.out.println(user.toString());

        try {
//            discoDataSource.saveOrUpdateUser(new User("SuperUserro", "UserName", "Passwood", "Email@mail.com"));
//
//            discoDataSource.saveOrUpdateUser(new User(1, "SuperUser", "UserName", "Passwd", "Email@mail.com"));

            discoDataSource.saveOrUpdateUserRelation(new Relation(1, 3, Relation.RELATION_TYPE_FRIEND));

            discoDataSource.saveOrUpdateUserRelation(new Relation(3, 1, Relation.RELATION_TYPE_BLOCKED));
            discoDataSource.saveOrUpdateUserRelation(new Relation(1, 3, Relation.RELATION_TYPE_BLOCKED));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        discoDataSource.close();
    }


}
