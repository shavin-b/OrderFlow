# download-deps.ps1
# Downloads all missing Maven JARs directly via PowerShell (bypasses Java's HTTP client).
# Run from d:\OrderFlow: .\download-deps.ps1

$m2 = "C:\Users\ASUS\.m2\repository"
$central = "https://repo.maven.apache.org/maven2"

function Download-Artifact {
    param(
        [string]$groupId,
        [string]$artifactId,
        [string]$version,
        [string]$classifier = "",
        [string]$ext = "jar"
    )
    $groupPath = $groupId.Replace(".", "\")
    $fileName  = if ($classifier) { "$artifactId-$version-$classifier.$ext" } else { "$artifactId-$version.$ext" }
    $localPath = "$m2\$groupPath\$artifactId\$version\$fileName"
    $url       = "$central/$($groupId.Replace('.','/'))" + "/$artifactId/$version/$fileName"

    if (Test-Path $localPath) {
        Write-Host "  [SKIP] $fileName already cached"
        return
    }

    $dir = Split-Path $localPath
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }

    try {
        Write-Host "  [DL]   $fileName"
        Invoke-WebRequest -Uri $url -OutFile $localPath -TimeoutSec 120 -ErrorAction Stop
        Write-Host "  [OK]   $fileName"
    } catch {
        Write-Host "  [ERR]  $fileName -> $($_.Exception.Message)"
        if (Test-Path $localPath) { Remove-Item $localPath -Force }
    }
}

Write-Host "`n=== Downloading missing Spring Framework JARs ==="
Download-Artifact "org.springframework" "spring-core"               "6.1.13"
Download-Artifact "org.springframework" "spring-jcl"                "6.1.13"
Download-Artifact "org.springframework" "spring-context"            "6.1.13"
Download-Artifact "org.springframework" "spring-aop"                "6.1.13"
Download-Artifact "org.springframework" "spring-beans"              "6.1.13"
Download-Artifact "org.springframework" "spring-expression"         "6.1.13"
Download-Artifact "org.springframework" "spring-web"                "6.1.13"
Download-Artifact "org.springframework" "spring-webmvc"             "6.1.13"
Download-Artifact "org.springframework" "spring-tx"                 "6.1.13"
Download-Artifact "org.springframework" "spring-orm"                "6.1.13"
Download-Artifact "org.springframework" "spring-jdbc"               "6.1.13"
Download-Artifact "org.springframework" "spring-test"               "6.1.13"

Write-Host "`n=== Downloading Spring Security JARs ==="
Download-Artifact "org.springframework.security" "spring-security-core"   "6.3.3"
Download-Artifact "org.springframework.security" "spring-security-web"    "6.3.3"
Download-Artifact "org.springframework.security" "spring-security-config" "6.3.3"
Download-Artifact "org.springframework.security" "spring-security-crypto" "6.3.3"
Download-Artifact "org.springframework.security" "spring-security-test"   "6.3.3"

Write-Host "`n=== Downloading Spring Data JARs ==="
Download-Artifact "org.springframework.data" "spring-data-commons" "3.3.4"
Download-Artifact "org.springframework.data" "spring-data-jpa"     "3.3.4"

Write-Host "`n=== Downloading Lombok ==="
Download-Artifact "org.projectlombok" "lombok" "1.18.34"

Write-Host "`n=== Downloading MapStruct ==="
Download-Artifact "org.mapstruct" "mapstruct"           "1.5.5.Final"
Download-Artifact "org.mapstruct" "mapstruct-processor" "1.5.5.Final"

Write-Host "`n=== Downloading Flyway ==="
Download-Artifact "org.flywaydb" "flyway-core"  "10.15.2"
Download-Artifact "org.flywaydb" "flyway-mysql" "10.15.2"

Write-Host "`n=== Downloading SpringDoc OpenAPI ==="
Download-Artifact "org.springdoc" "springdoc-openapi-starter-webmvc-ui"  "2.6.0"
Download-Artifact "org.springdoc" "springdoc-openapi-starter-webmvc-api" "2.6.0"
Download-Artifact "org.springdoc" "springdoc-openapi-starter-common"     "2.6.0"

Write-Host "`n=== Downloading Swagger Core ==="
Download-Artifact "io.swagger.core.v3" "swagger-core-jakarta"        "2.2.22"
Download-Artifact "io.swagger.core.v3" "swagger-annotations-jakarta" "2.2.22"
Download-Artifact "io.swagger.core.v3" "swagger-models-jakarta"      "2.2.22"
Download-Artifact "org.webjars"        "swagger-ui"                  "5.17.14"

Write-Host "`n=== Downloading Jakarta APIs ==="
Download-Artifact "jakarta.xml.bind"   "jakarta.xml.bind-api"   "4.0.2"
Download-Artifact "jakarta.activation" "jakarta.activation-api" "2.1.3"
Download-Artifact "jakarta.annotation" "jakarta.annotation-api" "2.1.1"
Download-Artifact "jakarta.persistence" "jakarta.persistence-api" "3.1.0"
Download-Artifact "jakarta.transaction" "jakarta.transaction-api" "2.0.1"
Download-Artifact "jakarta.validation" "jakarta.validation-api"  "3.0.2"

Write-Host "`n=== Downloading Jackson ==="
Download-Artifact "com.fasterxml.jackson.core"       "jackson-core"            "2.17.2"
Download-Artifact "com.fasterxml.jackson.core"       "jackson-databind"        "2.17.2"
Download-Artifact "com.fasterxml.jackson.core"       "jackson-annotations"     "2.17.2"
Download-Artifact "com.fasterxml.jackson.dataformat" "jackson-dataformat-yaml" "2.17.2"
Download-Artifact "com.fasterxml.jackson.datatype"   "jackson-datatype-jsr310" "2.17.2"

Write-Host "`n=== Downloading Commons & WebFlux deps ==="
Download-Artifact "org.apache.commons"           "commons-lang3"       "3.14.0"
Download-Artifact "io.projectreactor"            "reactor-core"        "3.6.10"
Download-Artifact "io.projectreactor.netty"      "reactor-netty-core"  "1.1.22"
Download-Artifact "io.projectreactor.netty"      "reactor-netty-http"  "1.1.22"
Download-Artifact "io.netty"                     "netty-all"           "4.1.113.Final"
Download-Artifact "io.micrometer"                "micrometer-core"     "1.13.4"

Write-Host "`n=== Downloading Spring Boot Config Processor ==="
Download-Artifact "org.springframework.boot" "spring-boot-configuration-processor" "3.3.4"

Write-Host "`n=== Downloading Hibernate ==="
Download-Artifact "org.hibernate.orm" "hibernate-core" "6.5.3.Final"

Write-Host "`n=== Downloading SLF4J & Logback ==="
Download-Artifact "org.slf4j"      "slf4j-api"         "2.0.16"
Download-Artifact "ch.qos.logback" "logback-classic"   "1.5.8"
Download-Artifact "ch.qos.logback" "logback-core"      "1.5.8"

Write-Host "`n=== Downloading MySQL Connector ==="
Download-Artifact "com.mysql" "mysql-connector-j" "8.4.0"

Write-Host "`n=== Downloading Lombok-MapStruct binding ==="
Download-Artifact "org.projectlombok" "lombok-mapstruct-binding" "0.2.0"

Write-Host "`n`n=== Download complete! Run: mvn clean compile -o ==="
