package com.yan.xpay.sui.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class SuiTransaction {

	private String digest;
	private TransactionWrapper transaction;
	private Effects effects;
	private List<Event> events;
	private List<ObjectChange> objectChanges;

	// 顶层 balanceChanges
	private List<BalanceChange> balanceChanges;

	private Long timestampMs;
	private Long checkpoint;
	private List<String> errors;

	// ------------------------------
	// Transaction wrapper
	// ------------------------------
	@Data
	public static class TransactionWrapper {
		private TransactionData data;
		private List<String> txSignatures;
	}

	// ------------------------------
	// Transaction -> data
	// ------------------------------
	@Data
	public static class TransactionData {
		private String messageVersion;
		private ProgrammableTransaction transaction;
		private String sender;
		private GasData gasData;
	}

	// ------------------------------
	// Programmable transaction
	// ------------------------------
	@Data
	public static class ProgrammableTransaction {
		private String kind;
		private List<Input> inputs;
		private List<Object> transactions; // MoveCall / SplitCoins 等结构复杂，交给 Map/Object
	}

	// ------------------------------
	// Inputs
	// ------------------------------
	@Data
	public static class Input {
		private String type;              // pure/object
		private String valueType;         // u64
		private String value;             // 3000000000
		private String objectType;        // sharedObject
		private String objectId;
		private String initialSharedVersion;
		private Boolean mutable;
	}

	// ------------------------------
	// Gas Data
	// ------------------------------
	@Data
	public static class GasData {
		private List<Payment> payment;
		private String owner;
		private String price;
		private String budget;
	}

	@Data
	public static class Payment {
		private String objectId;
		private Long version;
		private String digest;
	}

	// ------------------------------
	// Effects
	// ------------------------------
	@Data
	public static class Effects {
		private String messageVersion;
		private Status status;
		private String executedEpoch;
		private GasUsed gasUsed;
		private List<ModifiedAtVersion> modifiedAtVersions;
		private List<SharedObject> sharedObjects;
		private String transactionDigest;
		private List<Created> created;
		private List<Mutated> mutated;
		private GasObject gasObject;
		private List<String> dependencies;
		private String eventsDigest;
	}

	@Data
	public static class Status {
		private String status;
	}

	@Data
	public static class GasUsed {
		private BigDecimal computationCost;
		private BigDecimal storageCost;
		private BigDecimal storageRebate;
		private BigDecimal nonRefundableStorageFee;
	}

	@Data
	public static class ModifiedAtVersion {
		private String objectId;
		private String sequenceNumber;
	}

	@Data
	public static class SharedObject {
		private String objectId;
		private Long version;
		private String digest;
	}

	@Data
	public static class Created {
		private Owner owner;
		private Reference reference;
	}

	@Data
	public static class Mutated {
		private Owner owner;
		private Reference reference;
	}

	@Data
	public static class GasObject {
		private Owner owner;
		private Reference reference;
	}

	@Data
	public static class Reference {
		private String objectId;
		private Long version;
		private String digest;
	}

	// ------------------------------
	// Owner
	// ------------------------------
	@Data
	public static class Owner {
		// JSON 字段为 "AddressOwner"
		@Alias("AddressOwner")
		private String addressOwner;

		public Owner() {}

	}

	// ------------------------------
	// BalanceChanges（顶层）
	// ------------------------------
	@Data
	public static class BalanceChange {
		private Owner owner;
		private String coinType;
		private String amount;
	}

	// ------------------------------
	// Object Changes
	// ------------------------------
	@Data
	public static class ObjectChange {
		private String type;  // mutated/created
		private String sender;
		private Owner owner;
		private String objectType;
		private String objectId;
		private String version;
		private String previousVersion;
		private String digest;
	}

	// ------------------------------
	// Events
	// ------------------------------
	@Data
	public static class Event {
		private EventId id;
		private String packageId;
		private String transactionModule;
		private String sender;
		private String type;
		private Map<String, Object> parsedJson;
	}

	@Data
	public static class EventId {
		private String txDigest;
		private String eventSeq;
	}
}