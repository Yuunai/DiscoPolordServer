package discopolord.database;

import discopolord.entity.Relation;
import discopolord.entity.User;
import discopolord.protocol.Succ;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DiscoDataSource {

    Logger logger = Logger.getLogger(getClass().getSimpleName());

    /**
     * Database name
     */
    public static final String DB_NAME = "discopolord.db";

    /**
     * Database connection string
     */
    public static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/" + DB_NAME;

    /**
     * Database tables/culumns names
     */
    public static final String TABLE_USER = "user";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_IDENTIFIER = "identifier";
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_EMAIL = "email";

    public static final String TABLE_USER_RELATIONS = "user_relations";
    public static final String COLUMN_USER_RELATIONS_USER1_ID = "user1_id";
    public static final String COLUMN_USER_RELATIONS_USER2_ID = "user2_id";
    public static final String COLUMN_USER_RELATIONS_RELATION_TYPE = "relation_type";

    public static final int INDEX_USER_ID = 1;
    public static final int INDEX_USER_IDENTIFIER = 2;
    public static final int INDEX_USER_USERNAME = 3;
    public static final int INDEX_USER_PASSWORD = 4;
    public static final int INDEX_USER_EMAIL = 5;

    public static final int INDEX_USER_RELATIONS_USER1_ID = 1;
    public static final int INDEX_USER_RELATIONS_USER2_ID = 2;
    public static final int INDEX_USER_RELATIONS_RELATION_TYPE = 3;

    /** String used for users insertion */
    public static final String INSERT_USERS = "INSERT INTO " + TABLE_USER + "(" + COLUMN_USER_IDENTIFIER + ", "
            + COLUMN_USER_USERNAME + ", " + COLUMN_USER_PASSWORD + ", " + COLUMN_USER_EMAIL + ") VALUES (?, ?, ?, ?)";

    /** String used for user relations insertion */
    public static final String INSERT_USER_RELATIONS = "INSERT INTO " + TABLE_USER_RELATIONS + "(" + COLUMN_USER_RELATIONS_USER1_ID + ", "
            + COLUMN_USER_RELATIONS_USER2_ID + ", " + COLUMN_USER_RELATIONS_RELATION_TYPE + ") VALUES (?, ?, ?)";

    /** String used for user relation deletion */
    public static final String DELETE_USER_RELATION = "DELETE FROM " + TABLE_USER_RELATIONS + " WHERE " + COLUMN_USER_RELATIONS_USER1_ID
            + " = ? AND " + COLUMN_USER_RELATIONS_USER2_ID + " = ? ";

    private Connection conn;

    private PreparedStatement insertIntoUsers;
    private PreparedStatement insertIntoUserRelations;
    private PreparedStatement deleteUserRelation;

    private static DiscoDataSource instance = new DiscoDataSource();
    private DiscoDataSource() {

    }

    public static DiscoDataSource getInstance() {
        return instance;
    }


    public boolean open() {
        try {
//            TODO user, pass from props file
            conn = DriverManager.getConnection(CONNECTION_STRING, "dbproject", "dbproject");
            insertIntoUsers = conn.prepareStatement(INSERT_USERS);
            insertIntoUserRelations = conn.prepareStatement(INSERT_USER_RELATIONS);
            deleteUserRelation = conn.prepareStatement(DELETE_USER_RELATION);

            return true;
        } catch (SQLException e) {
            logger.warning("Couldn't open connection: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if(conn != null) {
                conn.close();
            }
            if(insertIntoUsers != null) {
                insertIntoUsers.close();
            }
            if(insertIntoUserRelations != null) {
                insertIntoUserRelations.close();
            }
            if(deleteUserRelation != null) {
                deleteUserRelation.close();
            }
        } catch (SQLException e) {
            logger.warning("Couldn't close connection: " + e.getMessage());
        }
    }

    /**
     * Returns user with given email and password, null in case if user doesn't exists. User password field is empty.
     * @param email User email
     * @param passwordHash User password hash
     * @return User with given email and password or null if user doesn't exists.
     */
    public User getUser(String email, String passwordHash) {
        User user = null;

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT *"
                     + " FROM " + TABLE_USER
                     + " WHERE " + COLUMN_USER_EMAIL + " = \'" + email +"\'"
                     + " AND " + COLUMN_USER_PASSWORD + " = \'" + passwordHash + "\'")) {

             while(results.next()) {
                 user = new User();
                 user.setUserId(results.getInt(INDEX_USER_ID));
                 user.setIdentifier(results.getString(INDEX_USER_IDENTIFIER));
                 user.setUsername(results.getString(INDEX_USER_USERNAME));
                 user.setEmail(results.getString(INDEX_USER_EMAIL));
             }

        } catch (SQLException e) {
            logger.warning("Querry failed: " + e.getMessage());
            return null;
        }

        return user;
    }

    /**
     *
     * @param identifier User identifier to be checked
     * @return True when identifier is free, false otherwise
     * @throws SQLException
     */
    public boolean checkIfIdentifierFree(String identifier) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT *"
                     + " FROM " + TABLE_USER
                     + " WHERE " + COLUMN_USER_IDENTIFIER + " = \'" + identifier + "\'")) {

            if(results.next())
                return false;
        } catch (SQLException e) {
            logger.warning("Querry failed: " + e.getMessage());
            throw e;
        }
        return true;
    }

    /**
     *
     * @param email Email to be checked
     * @return True when email is free, false otherwise
     * @throws SQLException
     */
    public boolean checkIfEmailFree(String email) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT *"
                     + " FROM " + TABLE_USER
                     + " WHERE " + COLUMN_USER_EMAIL + " = \'" + email + "\'")) {

            if(results.next())
                return false;
        } catch (SQLException e) {
            logger.warning("Querry failed: " + e.getMessage());
            throw e;
        }
        return true;
    }

    /**
     * Saves new user if user.getId() == null, else update existing user.
     * @param user user to be swaved or updated.
     */
    public void saveOrUpdateUser(User user) throws SQLException {

        if(user.getUserId() == 0) {
            insertIntoUsers.setString(1, user.getIdentifier());
            insertIntoUsers.setString(2, user.getUsername());
            insertIntoUsers.setString(3, user.getPassword());
            insertIntoUsers.setString(4, user.getEmail());
            insertIntoUsers.executeUpdate();
        } else {
            conn.createStatement().executeUpdate("UPDATE " + TABLE_USER
                    + " SET "
                    + COLUMN_USER_IDENTIFIER + " = \'" + user.getIdentifier() + "\', "
                    + COLUMN_USER_USERNAME + " = \'" +  user.getUsername() + "\', "
                    + COLUMN_USER_EMAIL + " = \'" + user.getEmail() + "\', "
                    + COLUMN_USER_PASSWORD + " = \'" + user.getPassword() + "\' "
                    + " WHERE " + COLUMN_USER_ID + " = " + user.getUserId());
        }

    }

    /**
     * Get relations for user with specified ID
     * @param id User id
     * @return Relations for user with specified id
     */
    public List<Succ.Message.UserStatus> getUserRelations(int id) {
        List<Succ.Message.UserStatus> result = new ArrayList<>();

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery(
                     "SELECT " + COLUMN_USER_IDENTIFIER + ", " + COLUMN_USER_USERNAME + ", " + COLUMN_USER_RELATIONS_RELATION_TYPE
                     + " FROM " + TABLE_USER_RELATIONS + " JOIN " + TABLE_USER_RELATIONS
                     + " ON " + COLUMN_USER_RELATIONS_USER2_ID + " = " + COLUMN_USER_ID
                     + " WHERE " + COLUMN_USER_RELATIONS_USER1_ID + " = " + id)) {

            while(results.next()) {
                result.add(Succ.Message.UserStatus.newBuilder()
                        .setIdentifier(results.getString(1))
                        .setUsername(results.getString(2))
                        .setStatus(Succ.Message.Status.forNumber(results.getInt(3)))
                        .build());
            }

        } catch (SQLException e) {
            logger.warning("Querry failed: " + e.getMessage());
            return null;
        }

        return result;
    }

    /**
     * Get relation between specified users
     * @param user1Id First user id
     * @param user2Id Second user id
     * @return Relation between specified users
     * @throws SQLException
     */
    private Relation getRelation(int user1Id, int user2Id) throws SQLException{
        Relation result = null;

        try (Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT *"
                    + " FROM " + TABLE_USER_RELATIONS
                    + " WHERE "
                    + COLUMN_USER_RELATIONS_USER1_ID + " = " + user1Id
                    + " AND "
                    + COLUMN_USER_RELATIONS_USER2_ID + " = " + user2Id);
            while(resultSet.next()) {
                result = new Relation();
                result.setUser1Id(resultSet.getInt(INDEX_USER_RELATIONS_USER1_ID));
                result.setUser2Id(resultSet.getInt(INDEX_USER_RELATIONS_USER2_ID));
                result.setRelationType(resultSet.getByte(INDEX_USER_RELATIONS_RELATION_TYPE));
            }
        } catch (SQLException e) {
            throw e;
        }

        return result;
    }

    /**
     * @param relation - relation to be saved or updated
     * @throws SQLException
     */
    public void saveOrUpdateUserRelation(Relation relation) throws SQLException {

        Relation rel = getRelation(relation.getUser1Id(), relation.getUser2Id());

        if(rel == null) {
            insertIntoUserRelations.setInt(1, relation.getUser1Id());
            insertIntoUserRelations.setInt(2, relation.getUser2Id());
            insertIntoUserRelations.setInt(3, relation.getRelationType());
            insertIntoUserRelations.executeUpdate();
        } else {
            conn.createStatement().executeUpdate("UPDATE " + TABLE_USER_RELATIONS
                    + " SET "
                    + COLUMN_USER_RELATIONS_RELATION_TYPE + " = " + relation.getRelationType()
                    + " WHERE " + COLUMN_USER_RELATIONS_USER1_ID + " = " + relation.getUser1Id()
                    + " AND " + COLUMN_USER_RELATIONS_USER2_ID + " = " + relation.getUser2Id());
        }
    }

    /**
     * Delete relation between given users
     * @param user1Id First user id
     * @param user2Id Second user id
     * @throws SQLException
     */
    public void deleteUserRelation(int user1Id, int user2Id) throws SQLException {
        deleteUserRelation.setInt(1, user1Id);
        deleteUserRelation.setInt(2, user2Id);
        deleteUserRelation.executeUpdate();
    }

}
