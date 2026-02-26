package com.lifeos.controller;

import com.lifeos.dto.ApiResponse;
import com.lifeos.entity.MusicPlaylist;
import com.lifeos.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 音乐播放列表Controller
 */
@RestController
@RequestMapping("/api/music")
@CrossOrigin(origins = "*")
public class MusicController {

    @Autowired
    private MusicService musicService;

    /**
     * 获取所有播放列表
     */
    @GetMapping("/playlists")
    public ApiResponse<List<MusicPlaylist>> getAllPlaylists() {
        try {
            List<MusicPlaylist> playlists = musicService.getAllPlaylists();
            return ApiResponse.success(playlists);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据情绪值获取匹配的播放列表
     */
    @GetMapping("/playlist/by-emotion")
    public ApiResponse<MusicPlaylist> getPlaylistByEmotion(@RequestParam Integer emotionScore) {
        try {
            MusicPlaylist playlist = musicService.getPlaylistByEmotion(emotionScore);
            if (playlist == null) {
                return ApiResponse.error("No playlist found for this emotion score");
            }
            return ApiResponse.success(playlist);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取默认播放列表
     */
    @GetMapping("/playlist/default")
    public ApiResponse<MusicPlaylist> getDefaultPlaylist() {
        try {
            MusicPlaylist playlist = musicService.getDefaultPlaylist();
            if (playlist == null) {
                return ApiResponse.error("No default playlist found");
            }
            return ApiResponse.success(playlist);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 添加播放列表
     */
    @PostMapping("/playlists")
    public ApiResponse<String> addPlaylist(@RequestBody MusicPlaylist playlist) {
        try {
            musicService.addPlaylist(playlist);
            return ApiResponse.success("Playlist added successfully");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新播放列表
     */
    @PutMapping("/playlists/{id}")
    public ApiResponse<String> updatePlaylist(@PathVariable Long id, @RequestBody MusicPlaylist playlist) {
        try {
            playlist.setId(id);
            musicService.updatePlaylist(playlist);
            return ApiResponse.success("Playlist updated successfully");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除播放列表
     */
    @DeleteMapping("/playlists/{id}")
    public ApiResponse<String> deletePlaylist(@PathVariable Long id) {
        try {
            musicService.deletePlaylist(id);
            return ApiResponse.success("Playlist deleted successfully");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
