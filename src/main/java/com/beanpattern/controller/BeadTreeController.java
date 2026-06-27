package com.beanpattern.controller;

import com.beanpattern.mapper.BeadAdminMapper;
import com.beanpattern.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bead")
public class BeadTreeController {

    private final BeadAdminMapper beadAdminMapper;

    public BeadTreeController(BeadAdminMapper beadAdminMapper) {
        this.beadAdminMapper = beadAdminMapper;
    }

    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> tree() {
        var brands = beadAdminMapper.listBrands();
        var kits = beadAdminMapper.listBrandKits();

        List<Map<String, Object>> tree = new ArrayList<>();
        for (var b : brands) {
            Map<String, Object> brandNode = new LinkedHashMap<>();
            brandNode.put("id", b.get("id"));
            brandNode.put("label", b.get("name"));
            brandNode.put("type", "brand");

            List<Map<String, Object>> kitChildren = new ArrayList<>();
            for (var k : kits) {
                if (!String.valueOf(b.get("id")).equals(String.valueOf(k.get("brandId")))) continue;

                Map<String, Object> kitNode = new LinkedHashMap<>();
                kitNode.put("id", k.get("id"));
                kitNode.put("label", k.get("colorCount") + "色套装");
                kitNode.put("type", "kit");
                kitNode.put("colorTotal", k.get("colorTotal"));
                kitChildren.add(kitNode);
            }
            brandNode.put("children", kitChildren);
            tree.add(brandNode);
        }
        return ApiResponse.ok(tree);
    }

    @GetMapping("/palettes/{id}/colors")
    public ApiResponse<List<Map<String, Object>>> paletteColors(@PathVariable Integer id) {
        return ApiResponse.ok(beadAdminMapper.listColorsByPaletteId(id));
    }

    @GetMapping("/brands/{id}/palettes")
    public ApiResponse<List<Map<String, Object>>> brandPalettes(@PathVariable Long id) {
        return ApiResponse.ok(beadAdminMapper.listKitsByBrandId(id));
    }
}
