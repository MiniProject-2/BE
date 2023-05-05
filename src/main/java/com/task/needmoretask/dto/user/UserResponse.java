package com.task.needmoretask.dto.user;

import com.task.needmoretask.model.profile.Profile;
import lombok.Getter;

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
}
