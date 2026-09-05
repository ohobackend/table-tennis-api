package com.tabletennis.app.domain.board.mapper;
import org.mapstruct.*;
import com.tabletennis.app.domain.board.Board;
import com.tabletennis.app.domain.board.dto.*;
@Mapper(componentModel="spring",unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface BoardMapper {
    BoardResponse response(Board entity);
    void update(BoardRequest request,@MappingTarget Board entity);
}
