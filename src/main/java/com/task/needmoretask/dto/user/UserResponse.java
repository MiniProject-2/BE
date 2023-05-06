package com.task.needmoretask.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.task.needmoretask.model.profile.Profile;
import com.task.needmoretask.model.user.User;
import lombok.Getter;

import java.util.List;

public class UserResponse {
    @Getter
    public static class ProfileOut{
        private Long profileId;
        private String profileImageUrl;

        public ProfileOut(Profile profile) {
            this.profileId = profile.getId();
            this.profileImageUrl = profile.getUrl();
        }
    }

    @Getter
    public static class UsersOut{
        private List<UserOut> users;
        @JsonProperty("isLast")
        private boolean isLast;

        @JsonIgnore
        private boolean last;

        public UsersOut(List<UserOut> users, boolean isLast) {
            this.users = users;
            this.isLast = isLast;
        }

        @Getter
        public static class UserOut{
            private Long userId;
            private String email;
            private String fullName;
            private User.Role role;
            private String profileImageUrl;

            public UserOut(User user) {
                this.userId = user.getId();
                this.email = user.getEmail();
                this.fullName = user.getFullname();
                this.role = user.getRole();
                this.profileImageUrl = user.getProfile().getUrl();
            }
        }
    }
}
