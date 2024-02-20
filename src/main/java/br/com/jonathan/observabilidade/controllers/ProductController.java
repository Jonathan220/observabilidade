package br.com.jonathan.observabilidade.controllers;

import br.com.jonathan.observabilidade.dtos.ProductRecordDto;
import br.com.jonathan.observabilidade.models.ProductModel;
import br.com.jonathan.observabilidade.respositories.ProductRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ProductController {
    @Autowired
    ProductRepository productRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @PostMapping("/products")
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto){
        logger.info("Salvando o produto");
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductModel>> getAllProducts(){
        logger.info("Obtendo lista de produtos");
        List<ProductModel> productsList = productRepository.findAll();
        if (!productsList.isEmpty()){
            for(ProductModel product : productsList){
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(productsList);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable(value = "id") UUID id){
        Optional<ProductModel> product = productRepository.findById(id);

        if (product.isEmpty()){
            logger.info("Produto com o id {} não encontrado", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        logger.info("Obtendo o produto id - {}, name - {}", product.get().getIdProduct(), product.get().getName());
        product.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Products list"));
        return ResponseEntity.status(HttpStatus.OK).body(product.get());
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id, @RequestBody @Valid ProductRecordDto productRecordDto){
        Optional<ProductModel> product = productRepository.findById(id);
        if (product.isEmpty()){
            logger.info("Produto com o id {} não encontrado", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        logger.info("Atualizando o produto id - {}, name - {}", product.get().getIdProduct(), product.get().getName());
        var productModel = product.get();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") UUID id){
        Optional<ProductModel> product = productRepository.findById(id);
        if(product.isEmpty()){
            logger.info("Produto com o id {} não encontrado", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        productRepository.delete(product.get());
        logger.info("Produto eliminado: id - {}, name - {}", product.get().getIdProduct(), product.get().getName());
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully.");
    }
}
