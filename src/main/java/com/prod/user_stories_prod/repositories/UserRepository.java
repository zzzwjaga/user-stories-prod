package com.prod.user_stories_prod.repositories;

import com.prod.user_stories_prod.entities.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<User> findById(UUID id) {
        String sql = """
                SELECT *
                FROM users
                WHERE id = :id
                """;
        return namedParameterJdbcTemplate.query(sql, Map.of("id", id), USER_ROW_MAPPER).stream().findFirst();
    }

    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT *
                FROM users
                WHERE username = :username
        """;
        List<User> users = namedParameterJdbcTemplate.query(sql, Map.of("username", username), USER_ROW_MAPPER);
        if(users.size() >1 ){
            throw new RuntimeException("More than one users found for this username" + username);
        }
        return  users.stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        String sql = """
                SELECT *
                FROM users
                WHERE email = :email
        """;
        List<User> users = namedParameterJdbcTemplate.query(sql, Map.of("email", email), USER_ROW_MAPPER);
        if(users.size() >1 ){
            throw new RuntimeException("More than one users found for this email" + email);
        }
        return  users.stream().findFirst();
    }

    public List<User> findAll(int page, int pageSize) {
       int offset = page * pageSize;
        String sql = """
                SELECT *
                FROM users
                ORDER BY username ASC
                LIMIT :limit
                OFFSET :offset
        """;
        return  namedParameterJdbcTemplate.query(sql, Map.of(
                "limit", pageSize,
                "offset", offset),
                USER_ROW_MAPPER);
    }

    public long countAll() {

        String sql = """
        SELECT COUNT(*)
        FROM users
        """;
        return namedParameterJdbcTemplate.queryForObject(
                sql,
                Map.of(),
                Long.class
        );
    }


    public boolean createUser(User user) {
        String sql = """
                INSERT INTO users(id, username, email, password_hash, created_at, updated_at, version)
                VALUES (:id, :username, :email, :password_hash, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
        """;
        Map<String,Object> params = Map.of(
                "id", user.id(),
                "username", user.username(),
                "email", user.email(),
                "password_hash", user.password_hash()
                        );
        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected > 0;
    }

    public boolean updateUser(User user) {
        String sql = """
                UPDATE users
                SET username = :username,
                email = :email,
                version = :version + 1,
                updated_at = CURRENT_TIMESTAMP
                WHERE id = :id AND version = :version
        """;
        Map<String,Object> params = Map.of(
                "id", user.id(),
                "username", user.username(),
                "email", user.email(),
                "version", user.version()
        );
        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected > 0;
    }

    public boolean deleteUser(UUID id) {
        String sql = """
                DELETE FROM users WHERE id = :id
        """;
        int rowsAffected = namedParameterJdbcTemplate.update(sql, Map.of("id", id));
        return rowsAffected > 0;
    }

    public void lockOnValue(Object value){
        String sql = "SELECT pg_advisory_xact_lock(hashtext(:lock));";
        namedParameterJdbcTemplate.queryForObject(sql, Map.of("lock", value.toString()), Object.class);
    }

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getObject("id", UUID.class),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at"),
            rs.getLong("version")
    );
}
