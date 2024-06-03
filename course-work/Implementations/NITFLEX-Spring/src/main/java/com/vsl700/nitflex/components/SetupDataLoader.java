package com.vsl700.nitflex.components;

import com.vsl700.nitflex.models.Privilege;
import com.vsl700.nitflex.models.Role;
import com.vsl700.nitflex.repo.PrivilegeRepository;
import com.vsl700.nitflex.repo.RoleRepository;
import com.vsl700.nitflex.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<Privilege> userPrivileges = new ArrayList<>(List.of(
                createPrivilegeIfNotFound("WATCH_CONTENT_PRIVILEGE"),
                createPrivilegeIfNotFound("READ_USER_SETTINGS_PRIVILEGE")
        ));

        List<Privilege> ownerPrivileges = new ArrayList<>(userPrivileges);
        ownerPrivileges.addAll(List.of(
                createPrivilegeIfNotFound("WRITE_USER_SETTINGS_PRIVILEGE"),
                createPrivilegeIfNotFound("MANAGE_USERS_PRIVILEGE"),
                createPrivilegeIfNotFound("DELETE_USERS_PRIVILEGE"),
                createPrivilegeIfNotFound("REGISTER_USERS_PRIVILEGE"),
                createPrivilegeIfNotFound("MANAGE_MOVIES_PRIVILEGE"),
                createPrivilegeIfNotFound("DELETE_MOVIES_PRIVILEGE")
        ));

        createRoleIfNotFound("ROLE_USER", userPrivileges);
        createRoleIfNotFound("ROLE_OWNER", ownerPrivileges);
    }

    //@Transactional
    private Privilege createPrivilegeIfNotFound(String name) {
        var result = privilegeRepository.findByName(name);
        if (result.isEmpty()) {
            var privilege = new Privilege(name);
            privilegeRepository.save(privilege);
            return privilege;
        }

        return result.get();
    }

    //@Transactional
    private Role createRoleIfNotFound(String name, Collection<Privilege> privileges) {
        var result = roleRepository.findByName(name);
        if (result.isEmpty()) {
            var role = new Role(name);
            role.setPrivileges(privileges.stream().toList());
            roleRepository.save(role);
            return role;
        }

        return result.get();
    }
}
