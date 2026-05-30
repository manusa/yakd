# If you update this file, please follow
# https://suva.sh/posts/well-documented-makefiles

.DEFAULT_GOAL := help

FRONTEND_DIR := src/main/frontend

# The help will print out all targets with their descriptions organized below their categories. The categories are represented by `##@` and the target descriptions by `##`.
# The awk command is responsible for reading the entire set of makefiles included in this invocation, looking for lines of the file as xyz: ## something, and then pretty-format the target and help. Then, if there's a line with ##@ something, that gets pretty-printed as a category.
# More info over the usage of ANSI control characters for terminal formatting: https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters
# More info over awk command: http://linuxcommand.org/lc3_adv_awk.php
#
# Notice that we have a little modification on the awk command to support slash in the recipe name:
# origin: /^[a-zA-Z_0-9-]+:.*?##/
# modified /^[a-zA-Z_0-9\/\.-]+:.*?##/
.PHONY: help
help: ## Display this help
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9\/\.-]+:.*?##/ { printf "  \033[36m%-21s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Build

.PHONY: build-frontend
build-frontend: ## Build the React frontend (Vite)
	cd $(FRONTEND_DIR) && npm install && npm run build

.PHONY: build-backend
build-backend: ## Build the backend JAR (assumes frontend pre-built)
	mvn -DskipTests clean package

.PHONY: build
build: ## Build everything (frontend + backend)
	mvn -Pbuild-frontend -DskipTests clean package

.PHONY: quick-build
quick-build: ## Run a fast "still wired together" check (skips frontend rebuild and Quarkus app build)
	mvn -T 1C -DskipTests -Dquarkus.build.skip package

.PHONY: native
native: ## Build native image (frontend + GraalVM native)
	mvn -Pbuild-frontend -Dnative clean package

##@ Test

.PHONY: test-app
test-app: ## Test the Quarkus app (Java unit + Selenium UI against the packaged app)
	mvn -Pbuild-frontend verify

.PHONY: test-unit
test-unit: ## Java unit tests only (Surefire — no frontend bundle, no Selenium/browser)
	mvn test

.PHONY: test-it
test-it: ## Selenium UI tests only (Failsafe + frontend bundle; set IT=Class[,Other] to filter)
	mvn -Pbuild-frontend verify -Dsurefire.skip=true $(if $(IT),-Dit.test='$(IT)',)

.PHONY: test-frontend
test-frontend: ## Frontend unit tests (Vitest — does not boot Quarkus or drive a browser)
	cd $(FRONTEND_DIR) && npm test

.PHONY: test
test: test-app test-frontend ## Run all tests (test-app + test-frontend)

.PHONY: check
check: test license-check ## Full local CI mirror — "am I ready to push?" (test + license-check)

##@ Development

.PHONY: dev-backend
dev-backend: ## Start Quarkus in dev mode (hot reload on :8080)
	mvn quarkus:dev

.PHONY: dev-frontend
dev-frontend: ## Start Vite dev server on :3000 (proxies /api to :8080 — run dev-backend alongside)
	cd $(FRONTEND_DIR) && npm start

.PHONY: fmt
fmt: ## Format frontend sources (prettier write)
	cd $(FRONTEND_DIR) && npm run prettier

.PHONY: fmt-check
fmt-check: ## Check frontend formatting without writing (symmetric to fmt)
	cd $(FRONTEND_DIR) && npm run prettier:check

.PHONY: typecheck
typecheck: ## Run frontend type checking (tsc on jsconfig.json)
	cd $(FRONTEND_DIR) && npm run typecheck

.PHONY: license-check
license-check: ## Check Apache 2.0 license headers on all source files
	.github/scripts/check-license-headers.sh

.PHONY: lint
lint: license-check ## Run ESLint + license-header check
	cd $(FRONTEND_DIR) && npm run eslint

##@ Deploy

.PHONY: deploy-minikube
deploy-minikube: ## Build image into the Minikube Docker daemon and apply k8s manifests
	eval $$(minikube docker-env) && mvn -Pbuild-frontend,k8s clean install

##@ Clean

.PHONY: clean-frontend
clean-frontend: ## Remove frontend build output and Vite cache
	rm -rf $(FRONTEND_DIR)/build $(FRONTEND_DIR)/node_modules/.vite

.PHONY: clean-backend
clean-backend: ## Remove backend build artifacts (target/)
	mvn clean

.PHONY: clean
clean: clean-frontend clean-backend ## Clean frontend and backend build artifacts

# Include additional make targets
-include build/*.mk
