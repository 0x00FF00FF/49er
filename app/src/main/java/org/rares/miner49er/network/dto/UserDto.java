package org.rares.miner49er.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String id;
    private String email;
    private String picture;
    private String name;
    private Role role;


    public enum Role {
        COMPANY_MANAGER(17),
        DEPARTMENT_MANAGER(16),
        PROJECT_MANAGER(15),
        ELEVATED_USER(10),
        DESIGNER(2),
        PROGRAMMER(1),
        USER(0);

        public final int value;

        Role(int value) {
            this.value = value;
        }
    }
}
