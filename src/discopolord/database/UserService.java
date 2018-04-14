package discopolord.database;

import discopolord.entity.Relation;
import discopolord.entity.User;
import discopolord.protocol.Succ;

import java.sql.SQLException;
import java.util.List;

public class UserService {

    private static DiscoDataSource discoDataSource;

    public UserService() {
        this.discoDataSource = DiscoDataSource.getInstance();
    }

    public User getUser(String email, String passwordHash) {
        return discoDataSource.getUser(email, passwordHash);
    }

    /**
     * Saves or update user
     * @param user User to be saved
     * @return 1 in case of success, -1 email and identifier already taken, -2 email taken, -3 identifier taken
     */
    public int saveOrUpdateUser(User user) {
        boolean emailFree = true;
        boolean identifierFree = true;

        try {
            if(user.getUserId() == 0) {
                emailFree = discoDataSource.checkIfEmailFree(user.getEmail());
                identifierFree = discoDataSource.checkIfIdentifierFree(user.getIdentifier());
            }

            if(emailFree && identifierFree) {
                discoDataSource.saveOrUpdateUser(user);
                return 1;
            } else if(!emailFree && !identifierFree) {
                return -1;
            } else if(!emailFree) {
                return -2;
            } else {
                return -3;
            }
        } catch(SQLException e) {
            return 0;
        }
    }

    public int saveOrUpdateUserRelation(Relation relation) {
        try {
            discoDataSource.saveOrUpdateUserRelation(relation);
        } catch (SQLException e) {
            return -1;
        }
        return 1;
    }

    public List<Succ.Message.UserStatus> getUserRelations(int userId) {
        return discoDataSource.getUserRelations(userId);
    }

    public int deleteUserRelation(int user1Id, int user2Id) {
        try {
            discoDataSource.deleteUserRelation(user1Id, user2Id);
        } catch (SQLException e) {
            return -1;
        }
        return 1;
    }

}
