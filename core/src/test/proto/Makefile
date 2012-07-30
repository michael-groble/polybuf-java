.PHONY: all java clean

POLYBUF_DIR = ../../../../proto/src/main/proto
PROTO_INCLUDES = -I/usr/local/include -I$(POLYBUF_DIR)

JAVA_GEN = ../java
TEST_GEN_DIR = $(JAVA_GEN)/polybuf/core/test
POLYBUF_GEN_DIR = $(JAVA_GEN)/polybuf/core/proto

all: java

java: $(POLYBUF_GEN_DIR)/Polybuf.java \
	$(TEST_GEN_DIR)/Coverage.java \
	$(TEST_GEN_DIR)/ExtensionsBase.java \
	$(TEST_GEN_DIR)/ExtensionsExt.java \
	$(TEST_GEN_DIR)/ExtensionsExtSamePackage.java \
	$(TEST_GEN_DIR)/Nested.java \
	$(TEST_GEN_DIR)/NoOptions.java \
	$(TEST_GEN_DIR)/PackagesPkg1.java \
	$(TEST_GEN_DIR)/PackagesPkg2.java \
	$(TEST_GEN_DIR)/Services.java \
	$(TEST_GEN_DIR)/VersionedV1.java \
	$(TEST_GEN_DIR)/VersionedV2.java


$(POLYBUF_GEN_DIR)/Polybuf.java: $(POLYBUF_DIR)/polybuf.proto
	protoc $(PROTO_INCLUDES) --java_out=$(JAVA_GEN) $(POLYBUF_DIR)/polybuf.proto

$(TEST_GEN_DIR)/Coverage.java: coverage.proto $(POLYBUF_DIR)/polybuf.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) -ocoverage.proto.pb --include_imports coverage.proto

$(TEST_GEN_DIR)/ExtensionsBase.java: extensions_base.proto $(POLYBUF_DIR)/polybuf.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) extensions_base.proto

$(TEST_GEN_DIR)/ExtensionsExt.java: extensions_ext.proto extensions_base.proto $(POLYBUF_DIR)/polybuf.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) extensions_ext.proto

$(TEST_GEN_DIR)/ExtensionsExtSamePackage.java: extensions_ext_same_package.proto extensions_base.proto $(POLYBUF_DIR)/polybuf.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) extensions_ext_same_package.proto

$(TEST_GEN_DIR)/Nested.java: nested.proto $(POLYBUF_DIR)/polybuf.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) nested.proto

$(TEST_GEN_DIR)/NoOptions.java: no_options.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) no_options.proto

$(TEST_GEN_DIR)/PackagesPkg1.java: packages_pkg1.proto $(POLYBUF_DIR)/polybuf.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) packages_pkg1.proto

$(TEST_GEN_DIR)/PackagesPkg2.java: packages_pkg2.proto packages_pkg1.proto $(POLYBUF_DIR)/polybuf.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) packages_pkg2.proto

$(TEST_GEN_DIR)/Services.java: services.proto no_options.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) services.proto

$(TEST_GEN_DIR)/VersionedV1.java: versioned_v1.proto $(POLYBUF_DIR)/polybuf.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) versioned_v1.proto

$(TEST_GEN_DIR)/VersionedV2.java: versioned_v2.proto $(POLYBUF_DIR)/polybuf.proto
	protoc $(PROTO_INCLUDES) -I. --java_out=$(JAVA_GEN) versioned_v2.proto

clean:
	rm -Rf $(TEST_GEN_DIR)/*.java
	rm -Rf $(POLYBUF_GEN_DIR)/Polybuf.java