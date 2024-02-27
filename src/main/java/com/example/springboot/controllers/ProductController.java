package com.example.springboot.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.springboot.dtos.ProductRecordDto;
import com.example.springboot.models.ProductModel;
import com.example.springboot.repositories.ProductRepository;

import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;



@RestController
public class ProductController {

	@Autowired
	ProductRepository productRepository;
	
	@PostMapping("/products")//CADASTRAR (SALVAR) UM PRODUTO
	public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto){
		var productModel = new ProductModel();
		BeanUtils.copyProperties(productRecordDto, productModel);
		return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel)); //HttpStatus.CREATED -> 201
	}
	
	@GetMapping("/products") //PEGAR TODOS OS PRODUTOS
	public ResponseEntity<List<ProductModel>> getAllProducts(){
		
		//HATEOAS  E CRIAÇÃO DE HIPERMÍDIAS 
		List<ProductModel> productList = productRepository.findAll();
		if (!productList.isEmpty()) {
			for(ProductModel product: productList) {
				UUID id = product.getIdProduct();
				product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());//CRIA UM LINK PARA OS ATRIBUTOS DE CADA UM DOS PRODUTOS DA LISTA.
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body(productList);//HttpStatus.OK -> 200
	}
	
	@GetMapping("/products/{id}")//PEGAR UM PRODUTO PELO ID
	public ResponseEntity<Object> getOneProduct(@PathVariable(value="id")UUID id){
		Optional<ProductModel> product0 = productRepository.findById(id);
		if (product0.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado.");//HttpStatus.NOT_FOUND -> 404
		}
		product0.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Lista de produtos"));//REDIRECIONA DE UM PRODUTO ESPECÍFICO PARA A LISTA COM TODOS OS PRODUTOS
		return ResponseEntity.status(HttpStatus.OK).body(product0.get());
	}
	
	@PutMapping("/products/{id}")//ATUALIZAR PRODUTO
	public ResponseEntity<Object> updateProduct(@PathVariable(value = "id")UUID id,
												@RequestBody @Valid ProductRecordDto productRecordDto){
	Optional<ProductModel> product0 = productRepository.findById(id);
	if (product0.isEmpty()) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado.");
	}
	var productModel = product0.get();
	BeanUtils.copyProperties(productRecordDto, productModel); //CONVERSÃO DO DTO PARA O PRODUCT MODEL
	return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(productModel));
	}
	
	@DeleteMapping("/products/{id}")//DELETAR PRODUTO
	public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id")UUID id){
	Optional<ProductModel> product0 = productRepository.findById(id);
	if (product0.isEmpty()) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produto não encontrado.");
	}
	productRepository.delete(product0.get());
	return ResponseEntity.status(HttpStatus.OK).body("Produto deletado com sucesso!");
	}
	
}
