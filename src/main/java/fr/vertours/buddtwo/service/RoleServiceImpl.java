package fr.vertours.buddtwo.service;

import fr.vertours.buddtwo.model.Role;
import fr.vertours.buddtwo.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role findUSERRole() {
        return roleRepository.findByName("USER");
    }

    public Role findAdminRole() {
        return roleRepository.findByName("ADMIN");
    }
}
