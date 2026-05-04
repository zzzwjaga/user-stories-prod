package com.prod.user_stories_prod.repositories;

import com.prod.user_stories_prod.entities.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public UserRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<User> findById(UUID id) {
        String sql = """
                SELECT username, email
                FROM users
                WHERE id = :id
                """;
        return namedParameterJdbcTemplate.query(sql, Map.of("id", id), USER_ROW_MAPPER).stream().findFirst();
    }

    public Optional<User> findByUsername(String username) {
        String sql = """
                SELECT username, email
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
                SELECT username, email
                FROM users
                WHERE email = :email
        """;
        List<User> users = namedParameterJdbcTemplate.query(sql, Map.of("email", email), USER_ROW_MAPPER);
        if(users.size() >1 ){
            throw new RuntimeException("More than one users found for this email" + email);
        }
        return  users.stream().findFirst();
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
            rs.getTimestamp("updated_at")
    );
}
