package com.tabletennis.app.domain.notice.mapper;
import org.mapstruct.*;
import com.tabletennis.app.domain.notice.Notice;
import com.tabletennis.app.domain.notice.dto.*;
@Mapper(componentModel="spring",unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface NoticeMapper {
    NoticeResponse response(Notice entity);
    void update(NoticeRequest request,@MappingTarget Notice entity);
}
