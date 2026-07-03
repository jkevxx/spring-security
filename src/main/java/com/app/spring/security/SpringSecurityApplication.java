package com.app.spring.security;

import com.app.spring.security.persistence.entity.PermissionEntity;
import com.app.spring.security.persistence.entity.RoleEntity;
import com.app.spring.security.persistence.entity.RoleEnum;
import com.app.spring.security.persistence.entity.UserEntity;
import com.app.spring.security.persistence.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Set;

// Tells Spring Boot to turn off its default, automatic database connection setup
//@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@SpringBootApplication()
public class SpringSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UserRepository userRepository) {
		return args -> {
			/* Create Permissions */
			PermissionEntity createPermission = PermissionEntity.builder()
					.name("CREATE")
					.build();
			PermissionEntity readPermission = PermissionEntity.builder()
					.name("READ")
					.build();
			PermissionEntity updatePermission = PermissionEntity.builder()
					.name("UPDATE")
					.build();
			PermissionEntity deletePermission = PermissionEntity.builder()
					.name("DELETE")
					.build();

			/* Create Roles*/
			RoleEntity roleAdmin = RoleEntity.builder()
					.roleEnum(RoleEnum.ADMIN)
					.permissions(Set.of(createPermission, readPermission, updatePermission, deletePermission))
					.build();

			RoleEntity roleUser = RoleEntity.builder()
					.roleEnum(RoleEnum.USER)
					.permissions(Set.of(createPermission, readPermission))
					.build();

			RoleEntity roleCustomer = RoleEntity.builder()
					.roleEnum(RoleEnum.CUSTOMER)
					.permissions(Set.of(createPermission, readPermission))
					.build();

			/* Create User */
			UserEntity userKevin = UserEntity.builder()
					.username("Kevin")
					.password("1234")
					.enabled(true)
					.accountNonExpired(true)
					.accountNonLocked(true)
					.credentialsNonExpired(true)
					.roles(Set.of(roleAdmin))
					.build();

			UserEntity userCustomer = UserEntity.builder()
					.username("Customer")
					.password("1234")
					.enabled(true)
					.accountNonExpired(true)
					.accountNonLocked(true)
					.credentialsNonExpired(true)
					.roles(Set.of(roleCustomer))
					.build();

			userRepository.saveAll(List.of(userKevin, userCustomer));
		};
	}
}
