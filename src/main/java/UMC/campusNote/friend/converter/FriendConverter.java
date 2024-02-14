package UMC.campusNote.friend.converter;

import UMC.campusNote.friend.entity.Friend;
import UMC.campusNote.user.entity.User;

public class FriendConverter {
    public static Friend fromEntity(User user1, User user2){
        return Friend.builder()
                .user1(user1)
                .user2(user2)
                .build();
    }
}