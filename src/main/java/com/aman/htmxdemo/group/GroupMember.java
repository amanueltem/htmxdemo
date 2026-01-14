package com.aman.htmxdemo.group;

import java.util.List;

import jakarta.persistence.*;
import com.aman.htmxdemo.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "GroupMember")
public class GroupMember {
	@Id
    @GeneratedValue
	private Long id;
	private String name;
    @OneToMany(mappedBy = "groupMember", orphanRemoval = true,fetch = FetchType.LAZY)
    private List<User> members;
}
