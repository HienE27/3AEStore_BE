// package com.duongthuantri.exercise201.controller;

// import java.util.List;
// import java.util.UUID;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import com.duongthuantri.exercise201.entity.Product;
// import com.duongthuantri.exercise201.entity.Tag;
// import com.duongthuantri.exercise201.service.ProductService;
// import com.duongthuantri.exercise201.service.TagService;

// @RestController
// @RequestMapping("/api/tags")
// public class TagController {

//     @Autowired
//     private TagService tagService;

//     @Autowired
//     private ProductService productService;

//     @GetMapping
//     public ResponseEntity<List<Tag>> getAllTags() {
//         return ResponseEntity.ok(tagService.findAll());
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<Tag> getTagById(@PathVariable UUID id) {
//         return tagService.findById(id)
//                 .map(ResponseEntity::ok)
//                 .orElse(ResponseEntity.notFound().build());
//     }

//     @PostMapping
//     public ResponseEntity<?> createTag(@RequestBody Tag tag) {
//         if (tagService.existsByTagName(tag.getTagName())) {
//             return ResponseEntity.badRequest()
//                     .body("Tag name already exists");
//         }
//         return ResponseEntity.ok(tagService.save(tag));
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<?> updateTag(
//             @PathVariable UUID id,
//             @RequestBody Tag tag) {
//         if (!tagService.existsById(id)) {
//             return ResponseEntity.notFound().build();
//         }
//         if (!tag.getTagName().equals(tagService.findById(id).get().getTagName()) &&
//                 tagService.existsByTagName(tag.getTagName())) {
//             return ResponseEntity.badRequest()
//                     .body("Tag name already exists");
//         }
//         tag.setId(id);
//         return ResponseEntity.ok(tagService.save(tag));
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
//         if (!tagService.existsById(id)) {
//             return ResponseEntity.notFound().build();
//         }
//         tagService.deleteById(id);
//         return ResponseEntity.ok().build();
//     }

//     @PostMapping("/{tagId}/products/{productId}")
//     public ResponseEntity<Void> addProductToTag(
//             @PathVariable UUID tagId,
//             @PathVariable UUID productId) {
//         Tag tag = tagService.findById(tagId).orElse(null);
//         Product product = productService.findById(productId).orElse(null);

//         if (tag == null || product == null) {
//             return ResponseEntity.notFound().build();
//         }

//         tagService.addProductToTag(tag, product);
//         return ResponseEntity.ok().build();
//     }

//     @DeleteMapping("/{tagId}/products/{productId}")
//     public ResponseEntity<Void> removeProductFromTag(
//             @PathVariable UUID tagId,
//             @PathVariable UUID productId) {
//         Tag tag = tagService.findById(tagId).orElse(null);
//         Product product = productService.findById(productId).orElse(null);

//         if (tag == null || product == null) {
//             return ResponseEntity.notFound().build();
//         }

//         tagService.removeProductFromTag(tag, product);
//         return ResponseEntity.ok().build();
//     }
// }