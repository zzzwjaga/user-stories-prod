package com.prod.user_stories_prod.repositories;

import com.prod.user_stories_prod.entities.Board;
import org.springframework.boot.web.servlet.filter.OrderedFormContentFilter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BoardRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final OrderedFormContentFilter orderedFormContentFilter;


    public BoardRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, OrderedFormContentFilter orderedFormContentFilter) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.orderedFormContentFilter = orderedFormContentFilter;
    }

    public List<Board> findAll(int page, int pageSize) {
        int offset = page * pageSize;

        String sql = """
            SELECT * 
            FROM boards
            ORDER BY boardname
            LIMIT :limit
            OFFSET :offset
            """;
        List<Board> boards = namedParameterJdbcTemplate.query(sql, Map.of("limit", pageSize, "offset", offset), BOARD_ROW_MAPPER);
        if(boards.isEmpty()) {
            return List.of();
        }
        return boards;
    }

    public List<Board> findByOwner(UUID owner_id,  int page, int size) {
        int offset = page * size;

        String sql = """
        SELECT * FROM boards 
        WHERE owner_id = :owner_id
        ORDER BY boardname
        LIMIT :limit
        OFFSET :offset
        """;
        List<Board> boardsByOwner = namedParameterJdbcTemplate.query(sql, Map.of("owner_id", owner_id, "limit", size, "offset", offset), BOARD_ROW_MAPPER);
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

    public Optional<Board> findByIdForUpdate(UUID id)
    {
        String sql = """
        SELECT *
        FROM boards
        WHERE id = :id
        FOR UPDATE
    """;

        return namedParameterJdbcTemplate.query(
                sql,
                Map.of("id", id),
                BOARD_ROW_MAPPER
        ).stream().findFirst();
    }

    public Optional<Board> findByName(UUID owner_id, String boardname) {
        String sql = "SELECT * FROM boards WHERE owner_id = :owner_id AND boardname = :boardname";
        List<Board> boards = namedParameterJdbcTemplate.query(sql, Map.of(
                "owner_id", owner_id,
                "boardname", boardname
                ), BOARD_ROW_MAPPER);
        if (boards.size() > 1) {
            throw new RuntimeException("More than one boards found by this owner_id: " + owner_id + "this boardname: " + boardname);
        }
        if (boards.isEmpty()) {
            return Optional.empty();
        }
        return boards.stream().findFirst();
    }

    public boolean createBoard(Board board) {
        String sql = """
         INSERT INTO boards (id, owner_id, boardname, description, created_at, updated_at, version)
         VALUES (:id, :owner_id, :boardname, :description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
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

    public boolean updateBoard(Board board){
        String sql = """
        UPDATE boards
            SET boardname = :boardname,
            description = :description,
            version = version + 1,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :id AND version = :version;
        """;
        int  rowsAffected = namedParameterJdbcTemplate.update(sql, Map.of(
                "id", board.id(),
                "boardname", board.boardname(),
                "description", board.description(),
                "version", board.version()
        ));
        return rowsAffected == 1;
    }

    public boolean deleteBoard(UUID id) {
        String sql = """
        DELETE FROM boards WHERE id = :id
        """;
        int  rowsAffected = namedParameterJdbcTemplate.update(sql, Map.of("id", id));
        return rowsAffected == 1;
    }

    public Long getNextStoryNumber(UUID board_id) {
        String sql = """
        UPDATE boards
        SET story_sequence = story_sequence+1
        WHERE id = :board_id
        RETURNING story_sequence
        """;

        return namedParameterJdbcTemplate.queryForObject(sql, Map.of("board_id", board_id), Long.class);
    }

    public long countAll() {

        String sql = """
        SELECT COUNT(*)
        FROM boards
        """;
        return namedParameterJdbcTemplate.queryForObject(
                sql,
                Map.of(),
                Long.class
        );
    }

    public long countAllByOwner(UUID owner_id) {

        String sql = """
        SELECT COUNT(*)
        FROM boards
        WHERE owner_id = :owner_id
        """;
        return namedParameterJdbcTemplate.queryForObject(
                sql,
                Map.of("owner_id", owner_id),
                Long.class
        );
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
            rs.getTimestamp("updated_at"),
            rs.getLong("version")
    );
}
