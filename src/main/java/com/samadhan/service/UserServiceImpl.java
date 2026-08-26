package com.samadhan.service;

import com.samadhan.entity.UserDetails;
import com.samadhan.repository.UserRepository;
import com.samadhan.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    UserRepository userrepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails findById(Long userId) {
       Optional<UserDetails> userList=userrepo.findById(userId);
        return userList.get();

    }

    @Override
    public Optional<UserDetails> findByUserContactNumber(String userContactNumber) {
        return userrepo.findByUserContactNumber(userContactNumber);
    }

	@Override
	public UserDetails loginUser(String userName, String password) {
		UserDetails user = userrepo.findByUserEmail(userName);

		if (user == null || user.getUserPassword() == null) {
			return null;
		}

		String storedPassword = user.getUserPassword();

		if (PasswordUtil.isBcryptHash(storedPassword)) {
			return passwordEncoder.matches(password, storedPassword) ? user : null;
		}

		if (!storedPassword.equals(password)) {
			return null;
		}

		// Legacy plaintext password — transparently migrate to a bcrypt hash on successful login,
		// same as TransferVendor login (see LoginService#loginTransfervendor).
		user.setUserPassword(passwordEncoder.encode(password));
		userrepo.save(user);
		return user;
	}
}
