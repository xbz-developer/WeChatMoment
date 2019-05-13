package com.thoughtworks.wechatmoment.bean;

import java.util.List;

public class TweetInfo {
    private String content;
    private List<ImageUrl> images;
    private Sender sender;
    private List<CommentInfo> comments;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ImageUrl> getImages() {
        return images;
    }

    public void setImages(List<ImageUrl> images) {
        this.images = images;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public List<CommentInfo> getComments() {
        return comments;
    }

    public void setComments(List<CommentInfo> comments) {
        this.comments = comments;
    }

    public static class ImageUrl {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Sender {
        private String username;
        private String nick;
        private String avatar;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }

    public static class CommentInfo {
        private String content;
        private TweetInfo.Sender sender;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Sender getSender() {
            return sender;
        }

        public void setSender(Sender sender) {
            this.sender = sender;
        }
    }
}
