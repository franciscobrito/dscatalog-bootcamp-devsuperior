package com.facbrito.dscatalog.services;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.facbrito.dscatalog.dto.RoleDTO;
import com.facbrito.dscatalog.dto.UserDTO;
import com.facbrito.dscatalog.dto.UserInsertDTO;
import com.facbrito.dscatalog.dto.UserUpdateDTO;
import com.facbrito.dscatalog.entities.Role;
import com.facbrito.dscatalog.entities.User;
import com.facbrito.dscatalog.repositories.RoleRepository;
import com.facbrito.dscatalog.repositories.UserRepository;
import com.facbrito.dscatalog.services.exceptions.DatabaseException;
import com.facbrito.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class UserService implements UserDetailsService {

	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository repository;

	@Autowired
	private RoleRepository roleRepository;

	@Transactional(readOnly = true)
	public Page<UserDTO> findAllPaged(PageRequest pageRequest) {
		Page<User> list = repository.findAll(pageRequest);
		return list.map(c -> new UserDTO(c));
	}

	@Transactional(readOnly = true)
	public UserDTO findById(Long id) {
		User user = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		return new UserDTO(user);
	}

	@Transactional
	public UserDTO insert(UserInsertDTO userDTO) {
		User user = new User();
		copyDtoToEntity(userDTO, user);
		user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		user = repository.save(user);
		return new UserDTO(user);
	}

	@Transactional
	public UserDTO update(Long id, UserUpdateDTO userUpdateDTO) {
		try {
			User user = repository.getOne(id);
			copyDtoToEntity(userUpdateDTO, user);
			user = repository.save(user);
			return new UserDTO(user);
		} catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("Id not found " + id);
		}
	}

	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("Id not found" + id);
		} catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Integrity violation");
		}
	}

	private void copyDtoToEntity(UserDTO dto, User entity) {
		entity.setFirstName(dto.getFirstName());
		entity.setLastName(dto.getLastName());
		entity.setEmail(dto.getEmail());

		entity.getRoles().clear();
		for (RoleDTO roleDto : dto.getRoles()) {
			Role role = roleRepository.getOne(roleDto.getId());
			entity.getRoles().add(role);
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = repository.findByEmail(username);
		if (user == null) {
			logger.error("User not found: " + username);
			throw new UsernameNotFoundException("Email not found");
		}
		logger.info("User found: " + username);
		return user;
	}

}
