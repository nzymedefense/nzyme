cargo clippy -- \
    -W clippy::all \
    -W clippy::pedantic \
    -D warnings \
    -A clippy::cast_sign_loss \
    -A clippy::similar_names \
    -A clippy::module_name_repetitions