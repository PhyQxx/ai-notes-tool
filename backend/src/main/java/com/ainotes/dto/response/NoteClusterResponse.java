package com.ainotes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteClusterResponse {
    private String clusterName;
    private List<Long> noteIds;
    private List<String> keywords;
}
