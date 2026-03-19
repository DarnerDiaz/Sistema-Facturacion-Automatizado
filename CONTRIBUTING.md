# 🤝 Guia de Contribucion

Gracias por contribuir a este proyecto.

## 🔒 Regla principal

La rama `main` esta protegida.

No se permiten pushes directos a `main`.
Todos los cambios deben entrar por Pull Request.

## 🌿 Flujo de trabajo obligatorio

1. Actualiza tu rama local principal:

```bash
git checkout main
git pull
```

2. Crea una rama para tu cambio:

```bash
git checkout -b tipo/descripcion-corta
```

Ejemplos de prefijo:

- `feat/` para funcionalidad nueva
- `fix/` para correccion de errores
- `docs/` para documentacion
- `chore/` para tareas de mantenimiento

3. Realiza tus cambios y confirma con mensajes claros:

```bash
git add .
git commit -m "tipo: descripcion breve"
```

4. Sube la rama y crea Pull Request:

```bash
git push -u origin tipo/descripcion-corta
```

5. Espera validaciones:

- CI en verde
- Al menos 1 aprobacion
- Conversaciones resueltas

6. Mergea el PR cuando cumpla todo lo anterior.

## ✅ Checklist para PR

- [ ] El cambio esta enfocado y no mezcla temas
- [ ] README o docs actualizados si aplica
- [ ] Variables de entorno documentadas si cambian
- [ ] CI pasa correctamente

## 🧪 Pruebas recomendadas antes de abrir PR

Backend:

```bash
cd backend
./mvnw -DskipTests compile
```

Frontend:

```bash
cd frontend
npm run build
```

## 📝 Convencion de commits sugerida

- `feat: ...`
- `fix: ...`
- `docs: ...`
- `refactor: ...`
- `chore: ...`

## 🙌 Gracias

Cada PR que mejora estabilidad, claridad o experiencia de uso suma mucho al proyecto.
