package com.aman.htmxdemo.user;
import com.aman.htmxdemo.deposit.Deposit;
import com.aman.htmxdemo.group.GroupMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "security_users")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails, Principal {

    @Id
    @GeneratedValue
    private Long id;

    private String fullName;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    @Column(unique = true,nullable = false)
    private String email;

    private String password;
    private boolean accountLocked;
    private boolean enabled;
    @ManyToMany(fetch=FetchType.EAGER)
    private List<Role> roles;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;
    private LocalDateTime lastLoggedIn;

    @Transient
    private Collection<? extends GrantedAuthority> authorities;


    @NotNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(r->new SimpleGrantedAuthority(r.getName()))
                .toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @NotNull
    @Override
    public String getUsername() {
        return email;
    }
    // From Principal
    @Override
    public String getName() {
        return email;
    }


    //relationships
    @ManyToOne
    @JoinColumn(name = "group_member_id")
    private GroupMember groupMember;
}

record RegisterRequest(
        @NotBlank String fullName,
        @Email String email,
        String phoneNumber,
        @Size(min = 8,message = "minimum password is 8 characters.")
        @NotBlank String password
) {}