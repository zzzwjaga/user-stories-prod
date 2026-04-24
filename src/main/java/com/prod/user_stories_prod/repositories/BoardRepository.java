package com.prod.user_stories_prod.repositories;

import com.prod.user_stories_prod.entities.Board;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BoardRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public BoardRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<Board> findAll() {
        String sql = "SELECT * FROM boards";
        List<Board> boards = namedParameterJdbcTemplate.query(sql, Map.of(), BOARD_ROW_MAPPER);
        if(boards.isEmpty()) {
            return List.of();
        }
        return boards;
    }

    public List<Board> findByOwner(UUID owner_id) {
        String sql = "SELECT * FROM boards WHERE owner_id = :owner_id";
        List<Board> boardsByOwner = namedParameterJdbcTemplate.query(sql, Map.of("owner_id", owner_id), BOARD_ROW_MAPPER);
        if(boardsByOwner.isEmpty()) {
            return List.of();
        }
        return boardsByOwner;
    }

    public Optional<Board> findById(UUID id) {
        String sql = "SELECT * FROM boards WHERE id = :id";
        List<Board> boards = namedParameterJdbcTemplate.query(sql, Map.of("id", id), BOARD_ROW_MAPPER);
        if (boards.size() > 1) {
            throw new RuntimeException("More than one boards found for this id: " + id);
        }
        if (boards.isEmpty()) {
            return Optional.empty();
        }
        return boards.stream().findFirst();
    }

    public boolean CreateBoard(Board board) {
        String sql = """
         INSERT INTO boards (id, owner_id, boardname, description, created_at, updated_at)
         VALUES (:id, :owner_id, :boardname, :description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
         """;

        Map<String, Object> params = Map.of(
                "id", board.id(),
                "owner_id", board.owner_id(),
                "boardname", board.boardname(),
                "description", board.description()
        );
        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        return rowsAffected == 1;
    }


    public void lockOnValue(Object value){
        String sql = "SELECT pg_advisory_xact_lock(hashtext(:lock));";
        namedParameterJdbcTemplate.queryForObject(sql, Map.of("lock", value.toString()), Object.class);
    }

    private static final RowMapper<Board> BOARD_ROW_MAPPER = (rs, rowNum) -> new Board(
            rs.getObject("id", UUID.class),
            rs.getObject("owner_id", UUID.class),
            rs.getString("boardname"),
            rs.getString("description"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
    );
}
