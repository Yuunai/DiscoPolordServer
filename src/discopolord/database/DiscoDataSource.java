package discopolord.database;

import discopolord.entity.Contact;
import discopolord.entity.User;
import discopolord.protocol.Succ;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class DiscoDataSource {

    private static Logger logger = Logger.getLogger(DiscoDataSource.class.getSimpleName());

    private static Properties props = new Properties();

    private boolean isConnectionOpen = false;

    private static final String DB_NAME_KEY = "database.name";

    private static final String DB_ADDRESS_KEY = "database.address";

    private static final String DB_USER_KEY = "database.user";

    private static final String DB_PASSOWRD_KEY = "database.password";

    private static String DB_NAME;

    private static String DB_ADDRESS;

    private static String DB_USER;

    private static String DB_PASSWORD;

    private static String CONNECTION_STRING;

    /**
     * Database tables/culumns names
     */
    public static final String TABLE_USER = "user";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_IDENTIFIER = "identifier";
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_EMAIL = "email";

    public static final String TABLE_USER_CONTACTS = "user_contacts";
    public static final String COLUMN_USER_CONTACTS_USER_ID = "user_id";
    public static final String COLUMN_USER_CONTACTS_CONTACT_ID = "contact_id";
    public static final String COLUMN_USER_CONTACTS_CONTACT_TYPE = "contact_type";

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
    public static final String INSERT_USER_RELATIONS = "INSERT INTO " + TABLE_USER_CONTACTS + "(" + COLUMN_USER_CONTACTS_USER_ID + ", "
            + COLUMN_USER_CONTACTS_CONTACT_ID + ", " + COLUMN_USER_CONTACTS_CONTACT_TYPE + ") VALUES (?, ?, ?)";

    /** String used for user relation deletion */
    public static final String DELETE_USER_RELATION = "DELETE FROM " + TABLE_USER_CONTACTS + " WHERE " + COLUMN_USER_CONTACTS_USER_ID
            + " = ? AND " + COLUMN_USER_CONTACTS_CONTACT_ID + " = ? ";

    private Connection conn;

    private PreparedStatement insertIntoUsers;
    private PreparedStatement insertIntoUserRelations;
    private PreparedStatement deleteUserRelation;

    private static DiscoDataSource instance = new DiscoDataSource();

    private DiscoDataSource() {
        initializeProperties();
        DB_NAME = props.getProperty(DB_NAME_KEY);
        DB_ADDRESS = props.getProperty(DB_ADDRESS_KEY);
        DB_USER = props.getProperty(DB_USER_KEY);
        DB_PASSWORD = props.getProperty(DB_PASSOWRD_KEY);

        CONNECTION_STRING = "jdbc:mysql://" + (DB_ADDRESS.endsWith("/") ? DB_ADDRESS : (DB_ADDRESS + "/")) + DB_NAME;
    }

    public static DiscoDataSource getInstance() {
        return instance;
    }


    public boolean open() {
        try {
            if (isConnectionOpen) {
                return true;
            }
            conn = DriverManager.getConnection(CONNECTION_STRING, DB_USER, DB_PASSWORD);
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
            isConnectionOpen = false;

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

    public User getUser(int id) {
        User user = null;

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT *"
                     + " FROM " + TABLE_USER
                     + " WHERE " + COLUMN_USER_ID + " = " + id)) {

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

    public User getUser(String identifier) {
        User user = null;

        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT *"
                     + " FROM " + TABLE_USER
                     + " WHERE " + COLUMN_USER_IDENTIFIER + " = \'" + identifier + "\'")) {

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
                     "SELECT u." + COLUMN_USER_IDENTIFIER + ", u." + COLUMN_USER_USERNAME + ", c." + COLUMN_USER_CONTACTS_CONTACT_TYPE
                     + " FROM " + TABLE_USER + " u JOIN " + TABLE_USER_CONTACTS
                     + " c ON c." + COLUMN_USER_CONTACTS_CONTACT_ID + " = u." + COLUMN_USER_ID
                     + " WHERE c." + COLUMN_USER_CONTACTS_USER_ID + " = " + id)) {

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
     * @return Contact between specified users
     * @throws SQLException
     */
    private Contact getRelation(int user1Id, int user2Id) throws SQLException{
        Contact result = null;

        try (Statement statement = conn.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT *"
                    + " FROM " + TABLE_USER_CONTACTS
                    + " WHERE "
                    + COLUMN_USER_CONTACTS_USER_ID + " = " + user1Id
                    + " AND "
                    + COLUMN_USER_CONTACTS_CONTACT_ID + " = " + user2Id);
            while(resultSet.next()) {
                result = new Contact();
                result.setUserId(resultSet.getInt(INDEX_USER_RELATIONS_USER1_ID));
                result.setContactId(resultSet.getInt(INDEX_USER_RELATIONS_USER2_ID));
                result.setContactType(resultSet.getByte(INDEX_USER_RELATIONS_RELATION_TYPE));
            }
        } catch (SQLException e) {
            throw e;
        }

        return result;
    }

    /**
     * @param contact - contact to be saved or updated
     * @throws SQLException
     */
    public void saveOrUpdateUserContact(Contact contact) throws SQLException {

        Contact rel = getRelation(contact.getUserId(), contact.getContactId());

        if(rel == null) {
            insertIntoUserRelations.setInt(1, contact.getUserId());
            insertIntoUserRelations.setInt(2, contact.getContactId());
            insertIntoUserRelations.setInt(3, contact.getContactType());
            insertIntoUserRelations.executeUpdate();
        } else {
            conn.createStatement().executeUpdate("UPDATE " + TABLE_USER_CONTACTS
                    + " SET "
                    + COLUMN_USER_CONTACTS_CONTACT_TYPE + " = " + contact.getContactType()
                    + " WHERE " + COLUMN_USER_CONTACTS_USER_ID + " = " + contact.getUserId()
                    + " AND " + COLUMN_USER_CONTACTS_CONTACT_ID + " = " + contact.getContactId());
        }
    }

    /**
     * Delete relation between given users
     * @param userId User id
     * @param contactId Contact id
     * @throws SQLException
     */
    public void deleteUserContact(int userId, int contactId) throws SQLException {
        deleteUserRelation.setInt(1, userId);
        deleteUserRelation.setInt(2, contactId);
        deleteUserRelation.executeUpdate();
    }

    public int getUserId(String identifier) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT " + COLUMN_USER_ID
                     + " FROM " + TABLE_USER
                     + " WHERE " + COLUMN_USER_IDENTIFIER + " = \'" + identifier + "\'")) {

            if(results.next()) {
                return results.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public static void initializeProperties() {
        logger.info("Loading database config file...");
        try {

            InputStream propsInput = new FileInputStream("database_config.properties");
            props.load(propsInput);
            logger.info("Database config file loaded...");
        } catch (FileNotFoundException e) {
            logger.warning("Database properties file not found: " + e.getMessage());
        } catch (IOException e) {
            logger.warning("Database properties file loading failed: " + e.getMessage());
        }
    }

}
