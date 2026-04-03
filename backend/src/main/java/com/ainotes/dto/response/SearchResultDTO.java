package com.ainotes.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

public class SearchResultDTO implements Serializable {
    private Long id;
    private String title;
    private String titleHighlight;
    private String contentPreview;
    private String contentType;
    private Integer isFavorite;
    private Integer isTop;
    private String tags;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private int matchCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTitleHighlight() { return titleHighlight; }
    public void setTitleHighlight(String titleHighlight) { this.titleHighlight = titleHighlight; }

    public String getContentPreview() { return contentPreview; }
    public void setContentPreview(String contentPreview) { this.contentPreview = contentPreview; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Boolean getIsFavorite() { return isFavorite != null && isFavorite == 1; }
    public void setIsFavorite(Integer isFavorite) { this.isFavorite = isFavorite; }

    public Boolean getIsTop() { return isTop != null && isTop == 1; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getMatchCount() { return matchCount; }
    public void setMatchCount(int matchCount) { this.matchCount = matchCount; }
}
