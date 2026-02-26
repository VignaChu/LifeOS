package com.lifeos.mapper;

import com.lifeos.entity.MusicPlaylist;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 音乐播放列表Mapper
 */
@Mapper
public interface MusicPlaylistMapper {

    @Select("SELECT * FROM music_playlists ORDER BY sort_order ASC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "neteasePlaylistId", column = "netease_playlist_id"),
        @Result(property = "emotionMin", column = "emotion_min"),
        @Result(property = "emotionMax", column = "emotion_max"),
        @Result(property = "isDefault", column = "is_default"),
        @Result(property = "sortOrder", column = "sort_order"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<MusicPlaylist> findAll();

    @Select("SELECT * FROM music_playlists WHERE id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "neteasePlaylistId", column = "netease_playlist_id"),
        @Result(property = "emotionMin", column = "emotion_min"),
        @Result(property = "emotionMax", column = "emotion_max"),
        @Result(property = "isDefault", column = "is_default"),
        @Result(property = "sortOrder", column = "sort_order"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    MusicPlaylist findById(Long id);

    @Select("SELECT * FROM music_playlists WHERE emotion_min <= #{emotionScore} AND emotion_max >= #{emotionScore} ORDER BY sort_order ASC LIMIT 1")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "neteasePlaylistId", column = "netease_playlist_id"),
        @Result(property = "emotionMin", column = "emotion_min"),
        @Result(property = "emotionMax", column = "emotion_max"),
        @Result(property = "isDefault", column = "is_default"),
        @Result(property = "sortOrder", column = "sort_order"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    MusicPlaylist findByEmotionScore(Integer emotionScore);

    @Select("SELECT * FROM music_playlists WHERE is_default = 1 LIMIT 1")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description"),
        @Result(property = "neteasePlaylistId", column = "netease_playlist_id"),
        @Result(property = "emotionMin", column = "emotion_min"),
        @Result(property = "emotionMax", column = "emotion_max"),
        @Result(property = "isDefault", column = "is_default"),
        @Result(property = "sortOrder", column = "sort_order"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    MusicPlaylist findDefault();

    @Insert("INSERT INTO music_playlists (name, description, netease_playlist_id, emotion_min, emotion_max, is_default, sort_order) " +
            "VALUES (#{name}, #{description}, #{neteasePlaylistId}, #{emotionMin}, #{emotionMax}, #{isDefault}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(MusicPlaylist playlist);

    @Update("UPDATE music_playlists SET name = #{name}, description = #{description}, " +
            "netease_playlist_id = #{neteasePlaylistId}, emotion_min = #{emotionMin}, " +
            "emotion_max = #{emotionMax}, is_default = #{isDefault}, sort_order = #{sortOrder} " +
            "WHERE id = #{id}")
    void update(MusicPlaylist playlist);

    @Delete("DELETE FROM music_playlists WHERE id = #{id}")
    void deleteById(Long id);

    @Update("UPDATE music_playlists SET is_default = 0 WHERE is_default = 1")
    void clearDefault();
}
