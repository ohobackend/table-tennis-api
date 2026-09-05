package com.tabletennis.app.domain.user.mapper;
import org.mapstruct.*;
import com.tabletennis.app.domain.user.User;
import com.tabletennis.app.domain.user.dto.*;
@Mapper(componentModel="spring",unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface UserMapper {
    UserProfile profile(User user);
    void update(UserUpdateRequest request,@MappingTarget User user);
}
