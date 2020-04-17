package com.ruckertech.inventory.web.rest;

import com.ruckertech.inventory.InventoryApp;
import com.ruckertech.inventory.domain.Item;
import com.ruckertech.inventory.domain.Location;
import com.ruckertech.inventory.repository.ItemRepository;
import com.ruckertech.inventory.repository.search.ItemSearchRepository;
import com.ruckertech.inventory.service.ItemService;
import com.ruckertech.inventory.service.dto.ItemDTO;
import com.ruckertech.inventory.service.mapper.ItemMapper;
import com.ruckertech.inventory.service.dto.ItemCriteria;
import com.ruckertech.inventory.service.ItemQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link ItemResource} REST controller.
 */
@SpringBootTest(classes = InventoryApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class ItemResourceIT {

    private static final String DEFAULT_ITEM_NAME = "AAAAAAAAAA";
    private static final String UPDATED_ITEM_NAME = "BBBBBBBBBB";

    private static final byte[] DEFAULT_IMAGE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_IMAGE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_IMAGE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_IMAGE_CONTENT_TYPE = "image/png";

    private static final String DEFAULT_TAG = "AAAAAAAAAA";
    private static final String UPDATED_TAG = "BBBBBBBBBB";

    private static final byte[] DEFAULT_QR_CODE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_QR_CODE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_QR_CODE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_QR_CODE_CONTENT_TYPE = "image/png";

    private static final byte[] DEFAULT_BAR_CODE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_BAR_CODE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_BAR_CODE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_BAR_CODE_CONTENT_TYPE = "image/png";

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private ItemService itemService;

    /**
     * This repository is mocked in the com.ruckertech.inventory.repository.search test package.
     *
     * @see com.ruckertech.inventory.repository.search.ItemSearchRepositoryMockConfiguration
     */
    @Autowired
    private ItemSearchRepository mockItemSearchRepository;

    @Autowired
    private ItemQueryService itemQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restItemMockMvc;

    private Item item;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Item createEntity(EntityManager em) {
        Item item = new Item()
            .itemName(DEFAULT_ITEM_NAME)
            .image(DEFAULT_IMAGE)
            .imageContentType(DEFAULT_IMAGE_CONTENT_TYPE)
            .tag(DEFAULT_TAG)
            .qrCode(DEFAULT_QR_CODE)
            .qrCodeContentType(DEFAULT_QR_CODE_CONTENT_TYPE)
            .barCode(DEFAULT_BAR_CODE)
            .barCodeContentType(DEFAULT_BAR_CODE_CONTENT_TYPE);
        return item;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Item createUpdatedEntity(EntityManager em) {
        Item item = new Item()
            .itemName(UPDATED_ITEM_NAME)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE)
            .tag(UPDATED_TAG)
            .qrCode(UPDATED_QR_CODE)
            .qrCodeContentType(UPDATED_QR_CODE_CONTENT_TYPE)
            .barCode(UPDATED_BAR_CODE)
            .barCodeContentType(UPDATED_BAR_CODE_CONTENT_TYPE);
        return item;
    }

    @BeforeEach
    public void initTest() {
        item = createEntity(em);
    }

    @Test
    @Transactional
    public void createItem() throws Exception {
        int databaseSizeBeforeCreate = itemRepository.findAll().size();

        // Create the Item
        ItemDTO itemDTO = itemMapper.toDto(item);
        restItemMockMvc.perform(post("/api/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isCreated());

        // Validate the Item in the database
        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeCreate + 1);
        Item testItem = itemList.get(itemList.size() - 1);
        assertThat(testItem.getItemName()).isEqualTo(DEFAULT_ITEM_NAME);
        assertThat(testItem.getImage()).isEqualTo(DEFAULT_IMAGE);
        assertThat(testItem.getImageContentType()).isEqualTo(DEFAULT_IMAGE_CONTENT_TYPE);
        assertThat(testItem.getTag()).isEqualTo(DEFAULT_TAG);
        assertThat(testItem.getQrCode()).isEqualTo(DEFAULT_QR_CODE);
        assertThat(testItem.getQrCodeContentType()).isEqualTo(DEFAULT_QR_CODE_CONTENT_TYPE);
        assertThat(testItem.getBarCode()).isEqualTo(DEFAULT_BAR_CODE);
        assertThat(testItem.getBarCodeContentType()).isEqualTo(DEFAULT_BAR_CODE_CONTENT_TYPE);

        // Validate the Item in Elasticsearch
        verify(mockItemSearchRepository, times(1)).save(testItem);
    }

    @Test
    @Transactional
    public void createItemWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = itemRepository.findAll().size();

        // Create the Item with an existing ID
        item.setId(1L);
        ItemDTO itemDTO = itemMapper.toDto(item);

        // An entity with an existing ID cannot be created, so this API call must fail
        restItemMockMvc.perform(post("/api/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Item in the database
        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeCreate);

        // Validate the Item in Elasticsearch
        verify(mockItemSearchRepository, times(0)).save(item);
    }


    @Test
    @Transactional
    public void getAllItems() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList
        restItemMockMvc.perform(get("/api/items?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(item.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemName").value(hasItem(DEFAULT_ITEM_NAME)))
            .andExpect(jsonPath("$.[*].imageContentType").value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE))))
            .andExpect(jsonPath("$.[*].tag").value(hasItem(DEFAULT_TAG)))
            .andExpect(jsonPath("$.[*].qrCodeContentType").value(hasItem(DEFAULT_QR_CODE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].qrCode").value(hasItem(Base64Utils.encodeToString(DEFAULT_QR_CODE))))
            .andExpect(jsonPath("$.[*].barCodeContentType").value(hasItem(DEFAULT_BAR_CODE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].barCode").value(hasItem(Base64Utils.encodeToString(DEFAULT_BAR_CODE))));
    }
    
    @Test
    @Transactional
    public void getItem() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get the item
        restItemMockMvc.perform(get("/api/items/{id}", item.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(item.getId().intValue()))
            .andExpect(jsonPath("$.itemName").value(DEFAULT_ITEM_NAME))
            .andExpect(jsonPath("$.imageContentType").value(DEFAULT_IMAGE_CONTENT_TYPE))
            .andExpect(jsonPath("$.image").value(Base64Utils.encodeToString(DEFAULT_IMAGE)))
            .andExpect(jsonPath("$.tag").value(DEFAULT_TAG))
            .andExpect(jsonPath("$.qrCodeContentType").value(DEFAULT_QR_CODE_CONTENT_TYPE))
            .andExpect(jsonPath("$.qrCode").value(Base64Utils.encodeToString(DEFAULT_QR_CODE)))
            .andExpect(jsonPath("$.barCodeContentType").value(DEFAULT_BAR_CODE_CONTENT_TYPE))
            .andExpect(jsonPath("$.barCode").value(Base64Utils.encodeToString(DEFAULT_BAR_CODE)));
    }


    @Test
    @Transactional
    public void getItemsByIdFiltering() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        Long id = item.getId();

        defaultItemShouldBeFound("id.equals=" + id);
        defaultItemShouldNotBeFound("id.notEquals=" + id);

        defaultItemShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultItemShouldNotBeFound("id.greaterThan=" + id);

        defaultItemShouldBeFound("id.lessThanOrEqual=" + id);
        defaultItemShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllItemsByItemNameIsEqualToSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where itemName equals to DEFAULT_ITEM_NAME
        defaultItemShouldBeFound("itemName.equals=" + DEFAULT_ITEM_NAME);

        // Get all the itemList where itemName equals to UPDATED_ITEM_NAME
        defaultItemShouldNotBeFound("itemName.equals=" + UPDATED_ITEM_NAME);
    }

    @Test
    @Transactional
    public void getAllItemsByItemNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where itemName not equals to DEFAULT_ITEM_NAME
        defaultItemShouldNotBeFound("itemName.notEquals=" + DEFAULT_ITEM_NAME);

        // Get all the itemList where itemName not equals to UPDATED_ITEM_NAME
        defaultItemShouldBeFound("itemName.notEquals=" + UPDATED_ITEM_NAME);
    }

    @Test
    @Transactional
    public void getAllItemsByItemNameIsInShouldWork() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where itemName in DEFAULT_ITEM_NAME or UPDATED_ITEM_NAME
        defaultItemShouldBeFound("itemName.in=" + DEFAULT_ITEM_NAME + "," + UPDATED_ITEM_NAME);

        // Get all the itemList where itemName equals to UPDATED_ITEM_NAME
        defaultItemShouldNotBeFound("itemName.in=" + UPDATED_ITEM_NAME);
    }

    @Test
    @Transactional
    public void getAllItemsByItemNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where itemName is not null
        defaultItemShouldBeFound("itemName.specified=true");

        // Get all the itemList where itemName is null
        defaultItemShouldNotBeFound("itemName.specified=false");
    }
                @Test
    @Transactional
    public void getAllItemsByItemNameContainsSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where itemName contains DEFAULT_ITEM_NAME
        defaultItemShouldBeFound("itemName.contains=" + DEFAULT_ITEM_NAME);

        // Get all the itemList where itemName contains UPDATED_ITEM_NAME
        defaultItemShouldNotBeFound("itemName.contains=" + UPDATED_ITEM_NAME);
    }

    @Test
    @Transactional
    public void getAllItemsByItemNameNotContainsSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where itemName does not contain DEFAULT_ITEM_NAME
        defaultItemShouldNotBeFound("itemName.doesNotContain=" + DEFAULT_ITEM_NAME);

        // Get all the itemList where itemName does not contain UPDATED_ITEM_NAME
        defaultItemShouldBeFound("itemName.doesNotContain=" + UPDATED_ITEM_NAME);
    }


    @Test
    @Transactional
    public void getAllItemsByTagIsEqualToSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where tag equals to DEFAULT_TAG
        defaultItemShouldBeFound("tag.equals=" + DEFAULT_TAG);

        // Get all the itemList where tag equals to UPDATED_TAG
        defaultItemShouldNotBeFound("tag.equals=" + UPDATED_TAG);
    }

    @Test
    @Transactional
    public void getAllItemsByTagIsNotEqualToSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where tag not equals to DEFAULT_TAG
        defaultItemShouldNotBeFound("tag.notEquals=" + DEFAULT_TAG);

        // Get all the itemList where tag not equals to UPDATED_TAG
        defaultItemShouldBeFound("tag.notEquals=" + UPDATED_TAG);
    }

    @Test
    @Transactional
    public void getAllItemsByTagIsInShouldWork() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where tag in DEFAULT_TAG or UPDATED_TAG
        defaultItemShouldBeFound("tag.in=" + DEFAULT_TAG + "," + UPDATED_TAG);

        // Get all the itemList where tag equals to UPDATED_TAG
        defaultItemShouldNotBeFound("tag.in=" + UPDATED_TAG);
    }

    @Test
    @Transactional
    public void getAllItemsByTagIsNullOrNotNull() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where tag is not null
        defaultItemShouldBeFound("tag.specified=true");

        // Get all the itemList where tag is null
        defaultItemShouldNotBeFound("tag.specified=false");
    }
                @Test
    @Transactional
    public void getAllItemsByTagContainsSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where tag contains DEFAULT_TAG
        defaultItemShouldBeFound("tag.contains=" + DEFAULT_TAG);

        // Get all the itemList where tag contains UPDATED_TAG
        defaultItemShouldNotBeFound("tag.contains=" + UPDATED_TAG);
    }

    @Test
    @Transactional
    public void getAllItemsByTagNotContainsSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        // Get all the itemList where tag does not contain DEFAULT_TAG
        defaultItemShouldNotBeFound("tag.doesNotContain=" + DEFAULT_TAG);

        // Get all the itemList where tag does not contain UPDATED_TAG
        defaultItemShouldBeFound("tag.doesNotContain=" + UPDATED_TAG);
    }


    @Test
    @Transactional
    public void getAllItemsByCarIsEqualToSomething() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);
        Location car = LocationResourceIT.createEntity(em);
        em.persist(car);
        em.flush();
        item.setCar(car);
        itemRepository.saveAndFlush(item);
        Long carId = car.getId();

        // Get all the itemList where car equals to carId
        defaultItemShouldBeFound("carId.equals=" + carId);

        // Get all the itemList where car equals to carId + 1
        defaultItemShouldNotBeFound("carId.equals=" + (carId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultItemShouldBeFound(String filter) throws Exception {
        restItemMockMvc.perform(get("/api/items?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(item.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemName").value(hasItem(DEFAULT_ITEM_NAME)))
            .andExpect(jsonPath("$.[*].imageContentType").value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE))))
            .andExpect(jsonPath("$.[*].tag").value(hasItem(DEFAULT_TAG)))
            .andExpect(jsonPath("$.[*].qrCodeContentType").value(hasItem(DEFAULT_QR_CODE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].qrCode").value(hasItem(Base64Utils.encodeToString(DEFAULT_QR_CODE))))
            .andExpect(jsonPath("$.[*].barCodeContentType").value(hasItem(DEFAULT_BAR_CODE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].barCode").value(hasItem(Base64Utils.encodeToString(DEFAULT_BAR_CODE))));

        // Check, that the count call also returns 1
        restItemMockMvc.perform(get("/api/items/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultItemShouldNotBeFound(String filter) throws Exception {
        restItemMockMvc.perform(get("/api/items?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restItemMockMvc.perform(get("/api/items/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }


    @Test
    @Transactional
    public void getNonExistingItem() throws Exception {
        // Get the item
        restItemMockMvc.perform(get("/api/items/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateItem() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        int databaseSizeBeforeUpdate = itemRepository.findAll().size();

        // Update the item
        Item updatedItem = itemRepository.findById(item.getId()).get();
        // Disconnect from session so that the updates on updatedItem are not directly saved in db
        em.detach(updatedItem);
        updatedItem
            .itemName(UPDATED_ITEM_NAME)
            .image(UPDATED_IMAGE)
            .imageContentType(UPDATED_IMAGE_CONTENT_TYPE)
            .tag(UPDATED_TAG)
            .qrCode(UPDATED_QR_CODE)
            .qrCodeContentType(UPDATED_QR_CODE_CONTENT_TYPE)
            .barCode(UPDATED_BAR_CODE)
            .barCodeContentType(UPDATED_BAR_CODE_CONTENT_TYPE);
        ItemDTO itemDTO = itemMapper.toDto(updatedItem);

        restItemMockMvc.perform(put("/api/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isOk());

        // Validate the Item in the database
        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeUpdate);
        Item testItem = itemList.get(itemList.size() - 1);
        assertThat(testItem.getItemName()).isEqualTo(UPDATED_ITEM_NAME);
        assertThat(testItem.getImage()).isEqualTo(UPDATED_IMAGE);
        assertThat(testItem.getImageContentType()).isEqualTo(UPDATED_IMAGE_CONTENT_TYPE);
        assertThat(testItem.getTag()).isEqualTo(UPDATED_TAG);
        assertThat(testItem.getQrCode()).isEqualTo(UPDATED_QR_CODE);
        assertThat(testItem.getQrCodeContentType()).isEqualTo(UPDATED_QR_CODE_CONTENT_TYPE);
        assertThat(testItem.getBarCode()).isEqualTo(UPDATED_BAR_CODE);
        assertThat(testItem.getBarCodeContentType()).isEqualTo(UPDATED_BAR_CODE_CONTENT_TYPE);

        // Validate the Item in Elasticsearch
        verify(mockItemSearchRepository, times(1)).save(testItem);
    }

    @Test
    @Transactional
    public void updateNonExistingItem() throws Exception {
        int databaseSizeBeforeUpdate = itemRepository.findAll().size();

        // Create the Item
        ItemDTO itemDTO = itemMapper.toDto(item);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restItemMockMvc.perform(put("/api/items")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(itemDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Item in the database
        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Item in Elasticsearch
        verify(mockItemSearchRepository, times(0)).save(item);
    }

    @Test
    @Transactional
    public void deleteItem() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);

        int databaseSizeBeforeDelete = itemRepository.findAll().size();

        // Delete the item
        restItemMockMvc.perform(delete("/api/items/{id}", item.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Item> itemList = itemRepository.findAll();
        assertThat(itemList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Item in Elasticsearch
        verify(mockItemSearchRepository, times(1)).deleteById(item.getId());
    }

    @Test
    @Transactional
    public void searchItem() throws Exception {
        // Initialize the database
        itemRepository.saveAndFlush(item);
        when(mockItemSearchRepository.search(queryStringQuery("id:" + item.getId())))
            .thenReturn(Collections.singletonList(item));
        // Search the item
        restItemMockMvc.perform(get("/api/_search/items?query=id:" + item.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(item.getId().intValue())))
            .andExpect(jsonPath("$.[*].itemName").value(hasItem(DEFAULT_ITEM_NAME)))
            .andExpect(jsonPath("$.[*].imageContentType").value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE))))
            .andExpect(jsonPath("$.[*].tag").value(hasItem(DEFAULT_TAG)))
            .andExpect(jsonPath("$.[*].qrCodeContentType").value(hasItem(DEFAULT_QR_CODE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].qrCode").value(hasItem(Base64Utils.encodeToString(DEFAULT_QR_CODE))))
            .andExpect(jsonPath("$.[*].barCodeContentType").value(hasItem(DEFAULT_BAR_CODE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].barCode").value(hasItem(Base64Utils.encodeToString(DEFAULT_BAR_CODE))));
    }
}
