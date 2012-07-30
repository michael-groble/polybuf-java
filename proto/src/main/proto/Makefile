.PHONY: all java clean

PROTO_INCLUDES = -I/usr/local/include -I.

POLYBUF_GEN_DIR = ../java

all: java

java: $(POLYBUF_GEN_DIR)/Polybuf.java

$(POLYBUF_GEN_DIR)/Polybuf.java: polybuf.proto
	protoc $(PROTO_INCLUDES) --java_out=$(POLYBUF_GEN_DIR) polybuf.proto

clean:
	rm -Rf $(POLYBUF_GEN_DIR)/Polybuf.java
