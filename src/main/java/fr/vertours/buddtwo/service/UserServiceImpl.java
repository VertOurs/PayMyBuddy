package fr.vertours.buddtwo.service;

import fr.vertours.buddtwo.exception.EmailNotPresentInApplicationException;
import fr.vertours.buddtwo.exception.EmailNotPresentInFriendsException;
import fr.vertours.buddtwo.security.MyUserDetails;
import fr.vertours.buddtwo.dto.*;
import fr.vertours.buddtwo.exception.EmailAlreadyPresentException;
import fr.vertours.buddtwo.exception.PasswordDoesNotMatchException;
import fr.vertours.buddtwo.model.Role;
import fr.vertours.buddtwo.model.User;
import fr.vertours.buddtwo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.vertours.buddtwo.dto.FriendDTO.getFriendDTOByUser;

@Service
public class UserServiceImpl implements RegistrationService,
        HomeUserService, ProfileUserService, ContactUserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleServiceImpl roleService;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           RoleServiceImpl roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @PostConstruct
    public void initAdminUser() {
       Role role = roleService.findAdminRole();
       User admin = new User("admin", "admin",
                "admin@mail.com",
                passwordEncoder.encode("admin"), role);
       if (userRepository.findByEmail(admin.getEmail()) == null) {
           userRepository.save(admin);
       }
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void addFriendByEmail(String userEmail, String friendEmail) {
        User user = userRepository.findByEmail(userEmail);
        User friend = isEmailPresentInApplication(friendEmail);
        user.getMyFriendList().add(friend);
        friend.getMyFriendList().add(user);
        userRepository.save(user);
    }
    private User isEmailPresentInApplication(String email) {
        Optional<User> isUserExist = Optional.ofNullable(
                userRepository.findByEmail(email));
        if(isUserExist.isEmpty()) {
            throw new EmailNotPresentInApplicationException(email);
        }
        return isUserExist.get();
    }

    @Transactional
    public void delFriendByEmail(String userEmail, String friendEmail) {
        User user = userRepository.findByEmail(userEmail);
        isEmailPresentInFriendList(friendEmail, user);
        User friend = userRepository.findByEmail(friendEmail);
        user.getMyFriendList().remove(friend);
        friend.getMyFriendList().remove(user);
        userRepository.save(user);

    }
    private void isEmailPresentInFriendList(String email, User user) {
        List<User> isFriendExist = user.getMyFriendList()
                .stream().filter(f -> f.getEmail().equals(email))
                .collect(Collectors.toList());
        if(isFriendExist.isEmpty()) {
            throw new EmailNotPresentInFriendsException(email);
        }

    }


    @Transactional
    public void saveUserByRegistrationDTO(RegistrationDTO regDTO) {
        isEmailAlreadyExistInDataBase(regDTO.getEmail());
        Role role = roleService.findUSERRole();
        User user = new User(regDTO.getFirstName(),
                regDTO.getLastName(),
                regDTO.getEmail(),
                passwordEncoder.encode(regDTO.getPassword()), role);
        userRepository.save(user);
    }

    private void isEmailAlreadyExistInDataBase(String email)  {
        Optional<User> isUserExist = Optional.ofNullable(
                userRepository.findByEmail(email));
        if(isUserExist.isPresent()) {
            throw new EmailAlreadyPresentException(email);
        }
    }

    public HomeDTO findHomeDTOByMyUserDetails(MyUserDetails myUD) {
        User user = userRepository.findByEmail(myUD.getUsername());
        HomeDTO dto = new HomeDTO();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setBalance(String.valueOf(user.getBuddyBalance()));
        if (user.getBankAccount() != null) {
            dto.setBankName(user.getBankAccount().getCustomizeName());
        }
        return dto;
    }

    @Override
    public ProfileDTO findProfileDTO(MyUserDetails myUD) {
        ProfileDTO dto = new ProfileDTO();
        dto.setFirstName(myUD.getUser().getFirstName());
        dto.setLastName(myUD.getUser().getLastName());
        dto.setEmail(myUD.getUser().getEmail());

        return dto;
    }

    @Override
    public void updatePassword(ChangePasswordDTO dto,
                               MyUserDetails myUserDetails) {
        isPasswordsMatch(dto, myUserDetails);
        User user = myUserDetails.getUser();
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    private void isPasswordsMatch(ChangePasswordDTO dto,
                                  MyUserDetails myUserDetails) {
        if (!dto.getOldPassword().equals(myUserDetails.getPassword())) {
            throw new PasswordDoesNotMatchException();
        }
    }

    public ContactDTO findContactDTO(MyUserDetails myUD) {

        myUD.setUser(userRepository.findByEmail(myUD.getUsername()));
        ContactDTO contactDTO = new ContactDTO();


        for (User user :myUD.getUser().getMyFriendList()) {
            contactDTO.getFriendDTOS().add(getFriendDTOByUser(user));
        }
        return contactDTO;
    }
}
