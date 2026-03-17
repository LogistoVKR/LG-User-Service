package kz.logisto.lguserservice.mapper;

import kz.logisto.lguserservice.data.entity.User;
import kz.logisto.lguserservice.data.model.UserModel;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface UserMapper {

  UserModel toModel(User user);
}
