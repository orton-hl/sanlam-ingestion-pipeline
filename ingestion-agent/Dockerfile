FROM python:3.11-slim

RUN pip install pdm

WORKDIR /app

COPY pyproject.toml pdm.lock ./

RUN pdm config python.use_venv false && pdm install --prod

COPY . .

EXPOSE 8085

CMD ["pdm", "run", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8085"]