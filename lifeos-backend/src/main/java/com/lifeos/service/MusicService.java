package com.lifeos.service;

import com.lifeos.entity.MusicPlaylist;
import com.lifeos.mapper.MusicPlaylistMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 音乐播放列表Service
 */
@Service
public class MusicService {

    @Autowired
    private MusicPlaylistMapper musicPlaylistMapper;

    /**
     * 获取所有播放列表
     */
    public List<MusicPlaylist> getAllPlaylists() {
        return musicPlaylistMapper.findAll();
    }

    /**
     * 根据情绪值获取匹配的播放列表
     */
    public MusicPlaylist getPlaylistByEmotion(Integer emotionScore) {
        MusicPlaylist playlist = musicPlaylistMapper.findByEmotionScore(emotionScore);
        if (playlist == null) {
            // 如果没有匹配的歌单，返回默认歌单
            playlist = musicPlaylistMapper.findDefault();
        }
        return playlist;
    }

    /**
     * 获取默认播放列表
     */
    public MusicPlaylist getDefaultPlaylist() {
        return musicPlaylistMapper.findDefault();
    }

    /**
     * 添加播放列表
     */
    public void addPlaylist(MusicPlaylist playlist) {
        // 如果设为默认，先取消其他默认歌单
        if (playlist.getIsDefault() != null && playlist.getIsDefault()) {
            musicPlaylistMapper.clearDefault();
        }
        musicPlaylistMapper.insert(playlist);
    }

    /**
     * 更新播放列表
     */
    public void updatePlaylist(MusicPlaylist playlist) {
        // 如果设为默认，先取消其他默认歌单
        if (playlist.getIsDefault() != null && playlist.getIsDefault()) {
            musicPlaylistMapper.clearDefault();
        }
        musicPlaylistMapper.update(playlist);
    }

    /**
     * 删除播放列表
     */
    public void deletePlaylist(Long id) {
        musicPlaylistMapper.deleteById(id);
    }
}
